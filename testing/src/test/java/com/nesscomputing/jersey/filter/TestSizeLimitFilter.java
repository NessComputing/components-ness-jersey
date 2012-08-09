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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import com.nesscomputing.config.Config;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.testing.CapturingHttpResponseHandler;
import com.nesscomputing.httpserver.HttpServer;
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
public class TestSizeLimitFilter
{
    @LifecycleRule
    public final LifecycleStatement lifecycleRule = LifecycleStatement.serviceDiscoveryLifecycle();

    private String baseUrl;

    private final Config sizeConfig = Config.getFixedConfig("ness.filter.max-body-size.com.nesscomputing.jersey.filter.SizeResource.doPostConfigured", "128");

    @Rule
    public IntegrationTestRule test = IntegrationTestRuleBuilder.defaultBuilder()
        .addService("http", sizeConfig, TweakedModule.forServiceModule(SizeFilteredModule.class))
        .addTestCaseModules(lifecycleRule.getLifecycleModule())
        .build(this);

    @Inject
    private HttpClient httpClient = null;

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

    CapturingHttpResponseHandler handler = new CapturingHttpResponseHandler();

    @Test
    public void testPostInheritSize() throws Exception {
        assertEquals(200, httpClient.post(baseUrl + "/post_inherit", handler)
                .setContent(new byte[512]).perform().getStatusCode());
        assertEquals(400, httpClient.post(baseUrl + "/post_inherit", handler)
                .setContent(new byte[2048]).perform().getStatusCode());
    }

    @Test
    public void testPostBigSucceeds() throws Exception {
        assertEquals(200, httpClient.post(baseUrl + "/post_big", handler)
                .setContent(new byte[2048]).perform().getStatusCode());
        assertEquals(400, httpClient.post(baseUrl + "/post_big", handler)
                .setContent(new byte[8192]).perform().getStatusCode());
    }

    @Test
    public void testPostSmallFails() throws Exception {
        assertEquals(200, httpClient.post(baseUrl + "/post_small", handler)
                .setContent(new byte[16]).perform().getStatusCode());
        assertEquals(400, httpClient.post(baseUrl + "/post_small", handler)
                .setContent(new byte[256]).perform().getStatusCode());
    }

    @Test
    public void testPostConfigured() throws Exception {
        assertEquals(200, httpClient.post(baseUrl + "/post_configured", handler)
                .setContent(new byte[48]).perform().getStatusCode());
        assertEquals(400, httpClient.post(baseUrl + "/post_configured", handler)
                .setContent(new byte[256]).perform().getStatusCode());
    }
}
