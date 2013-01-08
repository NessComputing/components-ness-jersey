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

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.servlet.ServletModule;

import com.nesscomputing.config.Config;
import com.nesscomputing.jersey.core.NessGuiceContainer;
import com.nesscomputing.jersey.core.NessResourceConfig;
import com.nesscomputing.jersey.exceptions.NessJerseyExceptionMapperModule;
import com.nesscomputing.jersey.wadl.NessWadlAnnotationsConfig;

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
        final JerseyConfig jerseyConfig = config.getBean(JerseyConfig.class);
        bind(JerseyConfig.class).toInstance(jerseyConfig);

        install (new NessJerseyExceptionMapperModule());

        bind(ResourceConfig.class).to(NessResourceConfig.class);

        NessJerseyBinder.bindResponseFilter(binder()).to(JsonUtf8ResponseFilter.class);

        if (jerseyConfig.isGzipEnabled()) {
            NessJerseyBinder.bindRequestFilter(binder()).to(GZIPContentEncodingFilter.class);
            NessJerseyBinder.bindResponseFilter(binder()).to(GZIPContentEncodingFilter.class);
        }

        if (jerseyConfig.isLoggingEnabled()) {
            NessJerseyBinder.bindRequestFilter(binder()).to(LoggingFilter.class);
            NessJerseyBinder.bindResponseFilter(binder()).to(LoggingFilter.class);
        }

        bind(GuiceContainer.class).to(NessGuiceContainer.class).in(Scopes.SINGLETON);
        String first = paths.get(0);
        String[] rest = paths.subList(1, paths.size()).toArray(new String[paths.size()-1]);
        serve(first, rest).with(GuiceContainer.class, getJerseyFeatures(jerseyConfig));
    }

    Map<String, String> getJerseyFeatures(final JerseyConfig jerseyConfig)
    {
        final Map<String, String> jerseyFeatures = Maps.newHashMap();
        jerseyFeatures.put(ResourceConfig.FEATURE_TRACE, Boolean.toString(jerseyConfig.isTraceEnabled()));
        jerseyFeatures.put(ResourceConfig.FEATURE_TRACE_PER_REQUEST, Boolean.toString(jerseyConfig.isTracePerRequestEnabled()));
        jerseyFeatures.put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.toString(!jerseyConfig.isGenerateWadlEnabled()));
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
