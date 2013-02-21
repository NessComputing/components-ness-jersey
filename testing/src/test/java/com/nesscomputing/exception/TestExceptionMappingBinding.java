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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nesscomputing.config.Config;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.guice.HttpClientModule;
import com.nesscomputing.httpclient.response.HttpResponseException;
import com.nesscomputing.httpclient.response.Valid2xxContentConverter;
import com.nesscomputing.jersey.NessJerseyServletModule;
import com.nesscomputing.lifecycle.junit.LifecycleRunner;
import com.nesscomputing.lifecycle.junit.LifecycleStatement;
import com.nesscomputing.testing.IntegrationTestRule;
import com.nesscomputing.testing.IntegrationTestRuleBuilder;
import com.nesscomputing.testing.tweaked.TweakedModule;

@RunWith(LifecycleRunner.class)
public class TestExceptionMappingBinding
{
    private static final Config EMPTY_CONFIG = Config.getEmptyConfig();

    @Rule
    public LifecycleStatement lifecycle = LifecycleStatement.serviceDiscoveryLifecycle();

    @Rule
    public IntegrationTestRule rule = IntegrationTestRuleBuilder.defaultBuilder()
        .addService("boom", TweakedModule.forServiceModule(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (new NessJerseyServletModule(EMPTY_CONFIG));
                install (new NessApiExceptionModule("boom"));
                NessApiExceptionBinder.of(binder(), "boom").registerExceptionClass(BoomException.class);
                bind (BoomResource.class);
            }
        }))
        .build(this, new AbstractModule() {
            @Override
            protected void configure()
            {
                install (lifecycle.getLifecycleModule());
                install (new HttpClientModule("with"));
                install (new HttpClientModule("without"));

                install (new NessApiExceptionModule("with"));
                NessApiExceptionBinder.of(binder(), "with").registerExceptionClass(BoomException.class);
            }
        });

    @Inject
    @Named("with")
    HttpClient mappingClient;

    @Inject
    @Named("without")
    HttpClient regularClient;

    @Test(expected=BoomException.class)
    public void testWithMapping() throws Exception
    {
        mappingClient.get(UriBuilder.fromUri(rule.locateService("boom")).path("/boom").build(), Valid2xxContentConverter.DEFAULT_FAILING_RESPONSE_HANDLER).perform();
    }

    @Test
    public void testNoMapping() throws Exception
    {
        try {
            regularClient.get(UriBuilder.fromUri(rule.locateService("boom")).path("/boom").build(), Valid2xxContentConverter.DEFAULT_FAILING_RESPONSE_HANDLER).perform();
        } catch (HttpResponseException e) {
            assertEquals(BOOM_STATUS.getStatusCode(), e.getStatusCode());
            return;
        }
        fail();
    }

    /** A status unlikely to be accidentally returned (i.e. not 500) */
    public static final StatusType BOOM_STATUS = Status.SERVICE_UNAVAILABLE;

    @Path("/boom")
    public static class BoomResource
    {
        @GET
        public String boom()
        {
            throw new BoomException();
        }
    }

    @ExceptionType("Boom")
    public static class BoomException extends NessApiException
    {
        BoomException()
        {
            this (ImmutableMap.of(
                    ERROR_TYPE, "Boom",
                    ERROR_SUBTYPE, "blam",
                    DETAIL, "boom"
                ));
        }

        protected BoomException(Map<String, ? extends Object> fields)
        {
            super(fields);
        }

        private static final long serialVersionUID = 1L;

        @Override
        public StatusType getStatus()
        {
            return BOOM_STATUS;
        }
    }
}
