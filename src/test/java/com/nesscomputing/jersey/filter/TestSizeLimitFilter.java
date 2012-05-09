package com.nesscomputing.jersey.filter;

import static org.junit.Assert.assertEquals;

import ness.testing.IntegrationTestRule;
import ness.testing.IntegrationTestRuleBuilder;
import ness.testing.ServiceDefinition;
import ness.testing.ServiceDefinitionBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.testing.CapturingHttpResponseHandler;
import com.nesscomputing.httpserver.HttpServer;
import com.nesscomputing.testing.lessio.AllowDNSResolution;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowNetworkAccess(endpoints= {"127.0.0.1:*"})
@AllowDNSResolution
public class TestSizeLimitFilter {

    private final ServiceDefinition serviceDefn = new ServiceDefinitionBuilder()
            .addModule(new SizeFilteredModule())
            .setConfig("ness.filter.max-body-size.com.nesscomputing.jersey.filter.SizeResource.doPostConfigured", "128")
            .build();

    @Rule
    public IntegrationTestRule test = new IntegrationTestRuleBuilder()
        .addService("size", serviceDefn)
        .build(this);

    @Inject
    HttpClient httpClient;

    private String baseUrl;

    @Before
    public void figureOutUrl() {
        HttpServer server = test.exposeBinding("size", Key.get(HttpServer.class));
        baseUrl = "http://localhost:" + server.getInternalHttpPort();
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
