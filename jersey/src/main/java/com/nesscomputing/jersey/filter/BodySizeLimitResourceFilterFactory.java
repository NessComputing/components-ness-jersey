/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.jersey.filter;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.ObjectUtils;

import com.nesscomputing.config.Config;
import com.nesscomputing.jersey.util.MaxSizeInputStream;
import com.nesscomputing.logging.Log;

/**
 * A {@link ResourceFilterFactory} which discovers all resource methods.  If they take
 * an input stream, they will wrap it in {@link MaxSizeInputStream} with the configured
 * size.  Configuration keys, with the highest priority last:
 * <table border=1>
 * <tr><th>Key</th><th>Default Value</th><th>Purpose</th></tr>
 * <tr><td>ness.filter.max-body-size</td><td>1MB</td><td>Global restriction / default</td></tr>
 * <tr><td>ness.filter.max-body-size.<i>full-class-name</i></td><td>-</td><td>Restricts all resource methods in the class</td></tr>
 * <tr><td>ness.filter.max-body-size.<i>full-class-name</i>.<i>method-name</i></td><td>Restricts a particular method</td></tr>
 * </table>
 */
@Singleton
public class BodySizeLimitResourceFilterFactory implements ResourceFilterFactory {

    private static final Log LOG = Log.findLog();

    private final Config config;
    private final long defaultSizeLimit;

    @Inject
    BodySizeLimitResourceFilterFactory(Config config) {
        this.config = config;
        NessJerseyFiltersConfig filterConfig = config.getBean(NessJerseyFiltersConfig.class);

        defaultSizeLimit = filterConfig.getMaxBodySize();

    }

    @Override
    public List<ResourceFilter> create(AbstractMethod am)
    {
        BodySizeLimit annotation = ObjectUtils.firstNonNull(
                am.getAnnotation(BodySizeLimit.class),
                am.getResource().getAnnotation(BodySizeLimit.class));

        final AbstractConfiguration ac = config.getConfiguration();
        final Method realMethod = am.getMethod();
        Class<?> resourceClass = am.getResource().getResourceClass();
        Map<String, Long> foundValues = Maps.newLinkedHashMap();
        do {
            final String classConfigKey = "ness.filter.max-body-size." + resourceClass.getName();
            try {
                resourceClass.getMethod(realMethod.getName(), realMethod.getParameterTypes());
            } catch (NoSuchMethodException | SecurityException e) {
                LOG.trace(e);
                continue;
            }
            final String methodKey = classConfigKey + "." + realMethod.getName();
            foundValues.put(methodKey, ac.getLong(methodKey, null));
            foundValues.put(classConfigKey, ac.getLong(classConfigKey, null));
        } while ((resourceClass = resourceClass.getSuperclass()) != null);

        if (annotation != null) {
            foundValues.put("annotation " + annotation, annotation.value());
        }
        foundValues.put("default", defaultSizeLimit);

        for (Entry<String, Long> e : foundValues.entrySet()) {
            final Long value = e.getValue();
            if (value != null) {

                String message = "[%s] => %s";
                Object[] args = {Joiner.on("], [").withKeyValueSeparator(" = ").useForNull("null").join(foundValues), value};

                if (value != defaultSizeLimit || annotation != null) {
                    LOG.debug(message, args);
                } else {
                    LOG.trace(message, args);
                }

                return Collections.<ResourceFilter>singletonList(new Filter(value));
            }
        }

        throw new IllegalStateException("No value found for " + am + ": " + foundValues);
    }

    private static class Filter implements ResourceFilter, ContainerRequestFilter {
        private final long maxSize;

        public Filter(long maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return null;
        }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            InputStream entityInputStream = request.getEntityInputStream();
            if (entityInputStream != null) {
                request.setEntityInputStream(new MaxSizeInputStream(entityInputStream, maxSize));
            }
            return request;
        }
    }
}
