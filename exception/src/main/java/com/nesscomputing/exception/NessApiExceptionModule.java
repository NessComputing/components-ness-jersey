package com.nesscomputing.exception;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import com.nesscomputing.httpclient.guice.HttpClientModule;

/**
 * Add support for mapping NessApiException subclasses to and from HTTP responses.
 */
public final class NessApiExceptionModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind (ResponseMapper.class);

        HttpClientModule.bindNewObserver(binder()).to(ExceptionObserver.class);
        MapBinder.newMapBinder(binder(), String.class, ExceptionReviver.class).permitDuplicates();
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof NessApiExceptionModule;
    }
}
