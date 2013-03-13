package com.nesscomputing.jersey.types;

import static org.junit.Assert.assertEquals;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.response.StringContentConverter;
import com.nesscomputing.jersey.types.DateParam;
import com.nesscomputing.lifecycle.junit.LifecycleRunner;
import com.nesscomputing.lifecycle.junit.LifecycleStatement;
import com.nesscomputing.service.discovery.testing.server.MockedDiscoveryService;
import com.nesscomputing.testing.IntegrationTestRule;
import com.nesscomputing.testing.IntegrationTestRuleBuilder;
import com.nesscomputing.testing.tweaked.TweakedModule;

@RunWith(LifecycleRunner.class)
public class DateParamTest
{
    @Rule
    public LifecycleStatement lifecycle = LifecycleStatement.serviceDiscoveryLifecycle();

    @Rule
    public IntegrationTestRule rule = IntegrationTestRuleBuilder.defaultBuilder()
        .addTweakedModules(new MockedDiscoveryService())
        .addService("datetest", TweakedModule.forServiceModule(new AbstractModule() {
            @Override
            protected void configure()
            {
                bind (DateToLongResource.class);
            }
        }))
        .addTestCaseModules(lifecycle.getLifecycleModule())
        .build(this);

    @Inject
    HttpClient httpClient;

    @Test
    public void testDateLong() throws Exception
    {
        DateTime when = new DateTime(1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        "srvc://datetest/date?date=" + when.getMillis(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    @Test
    public void testDateString() throws Exception
    {
        DateTime when = new DateTime(1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        "srvc://datetest/date?date=" + when.toString(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    @Test
    public void testDateTZString() throws Exception
    {
        DateTime when = new DateTime(1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        "srvc://datetest/date?date=" + when.withZone(DateTimeZone.forID("America/Los_Angeles")).toString(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    @Test
    public void testNull() throws Exception
    {
        assertEquals("asdf", httpClient.get(
                        "srvc://datetest/date",
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform());
    }

    @Test
    public void testNegativeDate() throws Exception
    {
        DateTime when = new DateTime(-1000);
        assertEquals(when.getMillis(),
                Long.parseLong(httpClient.get(
                        "srvc://datetest/date?date=" + when.getMillis(),
                        StringContentConverter.DEFAULT_RESPONSE_HANDLER).perform()));
    }

    @Path("/date")
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
