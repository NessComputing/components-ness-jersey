package com.nesscomputing.jersey;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.sun.jersey.guice.JerseyServletModule;

import com.nesscomputing.config.Config;
import com.nesscomputing.jersey.exceptions.NessJerseyExceptionMapperModule;
import com.nesscomputing.jersey.filter.BodySizeLimitResourceFilterFactory;
import com.nesscomputing.jersey.json.NessJacksonJsonProvider;

public class ServerBaseModule extends AbstractModule
{
    private final Config config;

    public ServerBaseModule(Config config)
    {
        this.config = config;
    }

    @Override
    protected void configure()
    {
        install(new JerseyServletModule());
        install(new NessJerseyServletModule(config, "/*"));
        install (new NessJerseyExceptionMapperModule());

        NessJerseyBinder.bindResourceFilterFactory(binder()).to(BodySizeLimitResourceFilterFactory.class);

        bind (NessJacksonJsonProvider.class);
    }
}
