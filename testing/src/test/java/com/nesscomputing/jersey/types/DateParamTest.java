package com.nesscomputing.jersey.types;

import static org.junit.Assert.assertEquals;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nesscomputing.config.Config;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.response.StringContentConverter;
import com.nesscomputing.httpserver.HttpServer;
import com.nesscomputing.jersey.ServerBaseModule;
import com.nesscomputing.jersey.types.DateParam;
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
public class DateParamTest
{
    @LifecycleRule
    public final LifecycleStatement lifecycleRule = LifecycleStatement.serviceDiscoveryLifecycle();

    private String baseUrl;

    @Rule
    public IntegrationTestRule test = IntegrationTestRuleBuilder.defaultBuilder()
        .addService("http", TweakedModule.forServiceModule(DateToLongWadlModule.class))
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

    @Test
    public void testDateLong() throws Exception
    {
        DateTime when = new DateTime(1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        baseUrl + "/datetest/date?date=" + when.getMillis(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    @Test
    public void testDateString() throws Exception
    {
        DateTime when = new DateTime(1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        baseUrl + "/datetest/date?date=" + when.toString(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    @Test
    public void testDateTZString() throws Exception
    {
        DateTime when = new DateTime(1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        baseUrl + "/datetest/date?date=" + when.withZone(DateTimeZone.forID("America/Los_Angeles")).toString(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    @Test
    public void testNull() throws Exception
    {
        assertEquals("asdf", httpClient.get(
                        baseUrl + "/datetest/date",
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform());
    }

    @Test
    public void testNegativeDate() throws Exception
    {
        DateTime when = new DateTime(-1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        baseUrl + "/datetest/date?date=" + when.getMillis(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    public static class DateToLongWadlModule extends AbstractModule {
        private final Config config;

        public DateToLongWadlModule(Config config)
        {
            this.config = config;
        }

        @Override
        protected void configure() {
            install (new ServerBaseModule(config));
            bind (DateToLongResource.class);
        }
    }

    @Path("/datetest/date")
    @Produces(MediaType.TEXT_PLAIN)
    public static class DateToLongResource
    {
        @GET
        public String dateToLong(@QueryParam("date") DateParam dateTime)
        {
            if (DateParam.getDateTime(dateTime) == null) {
                return "asdf";
            }
            return Long.toString(DateParam.getDateTime(dateTime).getMillis());
        }
    }
}
