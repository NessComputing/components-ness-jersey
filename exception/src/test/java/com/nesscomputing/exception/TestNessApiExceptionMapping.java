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
package com.nesscomputing.exception;

import static com.nesscomputing.exception.NessApiException.DETAIL;
import static com.nesscomputing.exception.NessApiException.ERROR_SUBTYPE;
import static com.nesscomputing.exception.NessApiException.ERROR_TYPE;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.internal.HttpClientMethod;
import com.nesscomputing.httpclient.response.Valid2xxContentConverter;
import com.nesscomputing.httpclient.testing.TestingHttpClientBuilder;

public class TestNessApiExceptionMapping
{

    @Inject
    @Named("test")
    ExceptionObserver observer;

    @Before
    public void setUp()
    {
        Guice.createInjector(new NessApiExceptionModule("test"), new AbstractModule() {
            @Override
            protected void configure()
            {
                NessApiExceptionBinder.of(binder(), "test").registerExceptionClass(TestingException.class);
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
