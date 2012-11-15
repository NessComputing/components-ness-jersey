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
package com.nesscomputing.jersey.exceptions;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

import com.nesscomputing.config.Config;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.response.StringContentConverter;
import com.nesscomputing.httpclient.testing.CapturingHttpResponseHandler;
import com.nesscomputing.httpserver.HttpServer;
import com.nesscomputing.jersey.ServerBaseModule;
import com.nesscomputing.lifecycle.junit.LifecycleRule;
import com.nesscomputing.lifecycle.junit.LifecycleRunner;
import com.nesscomputing.lifecycle.junit.LifecycleStatement;
import com.nesscomputing.testing.IntegrationTestRule;
import com.nesscomputing.testing.IntegrationTestRuleBuilder;
import com.nesscomputing.testing.lessio.AllowDNSResolution;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;
import com.nesscomputing.testing.tweaked.TweakedModule;

@AllowNetworkAccess(endpoints= {"127.0.0.1:*"})
@AllowDNSResolution
@RunWith(LifecycleRunner.class)
public class TestArgumentExceptionMapping
{
    @LifecycleRule
    public final LifecycleStatement lifecycleRule = LifecycleStatement.serviceDiscoveryLifecycle();

    private String baseUrl;

    private final Module badResourceModule = new AbstractModule() {
        @Override
        public void configure() {
            install (new ServerBaseModule(Config.getEmptyConfig()));
            bind(BadResource.class);
        }
    };

    @Rule
    public IntegrationTestRule test = IntegrationTestRuleBuilder.defaultBuilder()
        .addService("http", TweakedModule.forServiceModule(badResourceModule))
        .addTestCaseModules(NessJerseyExceptionMapperModule.class, lifecycleRule.getLifecycleModule(), badResourceModule)
        .build(this);

    private GuiceFilter guiceFilter = null;

    @Before
    public void setUp()
    {
        guiceFilter = test.exposeBinding("http", Key.get(GuiceFilter.class));
        final HttpServer server = test.exposeBinding("http", Key.get(HttpServer.class));

        baseUrl = "http://localhost:" + server.getConnectors().get("internal-http").getPort();
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(guiceFilter);
        guiceFilter.destroy();
    }


    @Path("/message")
    public static class BadResource {

        private final ObjectMapper mapper;

        @Inject
        BadResource(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Consumes("application/json")
        @Produces("application/json")
        @POST
        public String doSomething(MessageHolder something) throws Exception {
            if ("die".equals(something.getMessage())) {
                mapper.readTree("{\"messa");
            }
            return something.getMessage();
        }
    }

    public static class MessageHolder {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @Inject
    HttpClient client;

    @Test
    public void testMappingOkJson() throws Exception {
        final String result = client.post(URI.create(baseUrl + "/message"), StringContentConverter.DEFAULT_RESPONSE_HANDLER)
                .setContentType("application/json").setContent("{\"message\": \"foo\"}").perform();
        assertEquals("foo", result);
    }

    @Test
    public void testMappingBadJson() throws Exception {
        final HttpClientResponse response = client.post(URI.create(baseUrl + "/message"), new CapturingHttpResponseHandler())
                .setContentType("application/json").setContent("{\"messa").perform();
        assertEquals(response.getStatusText(), 400, response.getStatusCode());
    }

    @Test
    public void testMappingInternalError() throws Exception {
        final HttpClientResponse response = client.post(URI.create(baseUrl + "/message"), new CapturingHttpResponseHandler())
                .setContentType("application/json").setContent("{\"message\": \"die\"}").perform();
        assertEquals(response.getStatusText(), 500, response.getStatusCode());
    }
}
