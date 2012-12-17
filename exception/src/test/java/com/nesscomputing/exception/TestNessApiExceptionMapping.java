package com.nesscomputing.exception;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;

import org.junit.Test;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.internal.HttpClientMethod;
import com.nesscomputing.httpclient.response.Valid2xxContentConverter;
import com.nesscomputing.httpclient.testing.TestingHttpClientBuilder;
import static com.nesscomputing.exception.NessApiException.*;

public class TestNessApiExceptionMapping
{

    @Inject
    ExceptionObserver observer;

    @Before
    public void setUp()
    {
        Guice.createInjector(new NessApiExceptionModule(), new AbstractModule() {
            @Override
            protected void configure()
            {
                NessApiExceptionBinder.registerExceptionClass(binder(), TestingException.class);
            }
        }).injectMembers(this);
    }

    @ExceptionType("foo")
    @ExceptionSubtype("baz")
    static class TestingException extends NessApiException
    {
        protected TestingException(Map<String, ? extends Object> fields)
        {
            super(fields);
        }

        private static final long serialVersionUID = 1L;

        @Override
        public Status getStatus()
        {
            return Status.INTERNAL_SERVER_ERROR;
        }
    }

    @Test(expected=UnknownNessApiException.class)
    public void testUnknownException() throws Exception
    {
        final Map<String, ?> error = ImmutableMap.of("causes", ImmutableList.of(ImmutableMap.of(
                ERROR_TYPE, "foo",
                ERROR_SUBTYPE, "bar",
                DETAIL, "bogus!"
            )));

        final TestingHttpClientBuilder builder = new TestingHttpClientBuilder().withObjectMapper(new ObjectMapper());
        builder.on(HttpClientMethod.GET).of("/foo")
            .respondWith(Response.serverError().type(NessApiException.MEDIA_TYPE).entity(error));

        builder.withObserver(observer);

        try (final HttpClient httpClient = builder.build()) {
            httpClient.get("/foo", Valid2xxContentConverter.DEFAULT_FAILING_RESPONSE_HANDLER).perform();
        }
    }

    @Test(expected=TestingException.class)
    public void testKnownException() throws Exception
    {
        final Map<String, ?> error = ImmutableMap.of("causes", ImmutableList.of(ImmutableMap.of(
                ERROR_TYPE, "foo",
                ERROR_SUBTYPE, "baz",
                DETAIL, "bogus!"
            )));

        final TestingHttpClientBuilder builder = new TestingHttpClientBuilder().withObjectMapper(new ObjectMapper());
        builder.on(HttpClientMethod.GET).of("/foo")
            .respondWith(Response.serverError().type(NessApiException.MEDIA_TYPE).entity(error));

        builder.withObserver(observer);

        try (final HttpClient httpClient = builder.build()) {
            httpClient.get("/foo", Valid2xxContentConverter.DEFAULT_FAILING_RESPONSE_HANDLER).perform();
        }
    }
}
