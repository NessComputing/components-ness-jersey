package com.nesscomputing.jersey.exceptions;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.nesscomputing.jersey.exceptions.GuiceProvisionExceptionMapper;

public class TestGuiceProvisionExceptionMapper
{
    @Test
    public void testNullCauseMessage()
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new Module() {
                                                           @Override
                                                           public void configure(final Binder binder) {
                                                               binder.bind(GuiceProvisionExceptionMapper.class).asEagerSingleton();
                                                           }
                                                       });

        final GuiceProvisionExceptionMapper mapper = injector.getInstance(GuiceProvisionExceptionMapper.class);

        final Throwable cause = new NullPointerException();
        final ProvisionException pe = new ProvisionException("hello, world", cause);

        final Response response = mapper.toResponse(pe);

        Assert.assertNotNull(response);
    }
}
