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
package com.nesscomputing.jersey;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.nesscomputing.config.Config;
import com.nesscomputing.jersey.exceptions.NessJerseyExceptionMapperModule;
import com.nesscomputing.jersey.wadl.NessWadlAnnotationsConfig;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

public class NessJerseyServletModule extends ServletModule
{
    private final Config config;
    private final List<String> paths;

    public NessJerseyServletModule(final Config config)
    {
        this(config, "/*");
    }

    public NessJerseyServletModule(Config config, String... paths) {
        this(config, Arrays.asList(paths));
    }

    public NessJerseyServletModule(Config config, List<String> paths) {
        Preconditions.checkNotNull(config, "null config");
        Preconditions.checkNotNull(paths, "null paths");
        Preconditions.checkArgument(paths.size() >= 1, "must serve at least one path");
        this.config = config;
        this.paths = ImmutableList.copyOf(paths);
    }

    @Override
    protected void configureServlets()
    {
        install (new NessJerseyExceptionMapperModule());

        bind(ExtensibleRequestFilter.class).in(Scopes.SINGLETON);
        bind(GuiceContainer.class).in(Scopes.SINGLETON);
        String first = paths.get(0);
        String[] rest = paths.subList(1, paths.size()).toArray(new String[paths.size()-1]);
        serve(first, rest).with(GuiceContainer.class, getJerseyFeatures(config));
    }

    Map<String, String> getJerseyFeatures(final Config config)
    {
        // This should be in a module
        final JerseyConfig jerseyConfig = config.getBean(JerseyConfig.class);

        final List<String> requestFilters = Lists.newArrayList();
        final List<String> responseFilters = Lists.newArrayList();

        responseFilters.add(JsonUtf8ResponseFilter.class.getName());

        if (jerseyConfig.isGzipEnabled()) {
            requestFilters.add(GZIPContentEncodingFilter.class.getName());
            responseFilters.add(GZIPContentEncodingFilter.class.getName());
        }

        if (jerseyConfig.isLoggingEnabled()) {
            requestFilters.add(LoggingFilter.class.getName());
            responseFilters.add(LoggingFilter.class.getName());
        }

        requestFilters.add(ExtensibleRequestFilter.class.getName());
        responseFilters.add(ExtensibleResponseFilter.class.getName());

        final Map<String, String> jerseyFeatures = Maps.newHashMap();
        jerseyFeatures.put(ResourceConfig.FEATURE_TRACE, Boolean.toString(jerseyConfig.isTraceEnabled()));
        jerseyFeatures.put(ResourceConfig.FEATURE_TRACE_PER_REQUEST, Boolean.toString(jerseyConfig.isTracePerRequestEnabled()));
        jerseyFeatures.put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.toString(!jerseyConfig.isGenerateWadlEnabled()));

        if (!responseFilters.isEmpty()) {
            jerseyFeatures.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, StringUtils.join(responseFilters, ","));
        }

        if (!requestFilters.isEmpty()) {
            jerseyFeatures.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,  StringUtils.join(requestFilters, ","));
        }

        jerseyFeatures.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, ExtensibleResourceFilterFactory.class.getName());

        // Expose the @RequiresAuthentication annotation
        jerseyFeatures.put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, NessWadlAnnotationsConfig.class.getName());

        return jerseyFeatures;
    }

    /**
     * @deprecated Use {@link NessJerseyBinder#bindRequestFilter(Binder)}.
     */
    @Deprecated
    public static final LinkedBindingBuilder<ContainerRequestFilter> addRequestFilterBinding(Binder binder) {
        return NessJerseyBinder.bindRequestFilter(binder);
    }

    /**
     * @deprecated Use {@link NessJerseyBinder#bindResponseFilter(Binder)}.
     */
    @Deprecated
    public static LinkedBindingBuilder<ContainerResponseFilter> addResponseFilterBinding(Binder binder) {
        return NessJerseyBinder.bindResponseFilter(binder);
    }

    /**
     * @deprecated Use {@link NessJerseyBinder#bindResourceFilterFactory(Binder)}.
     */
    @Deprecated
    public static LinkedBindingBuilder<ResourceFilterFactory> addResourceFilterFactoryBinding(Binder binder) {
        return NessJerseyBinder.bindResourceFilterFactory(binder);
    }

    /**
     * Binds the JacksonJsonProvider to Jersey.
     */
    @Provides
    @Singleton
    JacksonJsonProvider getJacksonJsonProvider(final ObjectMapper objectMapper)
    {
        final JacksonJsonProvider provider = new JacksonJsonProvider();
        provider.setMapper(objectMapper);
        return provider;
    }
}
