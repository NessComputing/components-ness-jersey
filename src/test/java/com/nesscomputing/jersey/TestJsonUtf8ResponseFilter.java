package com.nesscomputing.jersey;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.jersey.JsonUtf8ResponseFilter;
import com.sun.jersey.spi.container.ContainerResponse;

public class TestJsonUtf8ResponseFilter
{
    @Test
    public void testSimple()
    {
        final JsonUtf8ResponseFilter filter = new JsonUtf8ResponseFilter();

        final ContainerResponse c = new ContainerResponse(null, null, null);
        c.setResponse(Response.ok().type(MediaType.APPLICATION_JSON_TYPE).build());

        final ContainerResponse r2 = filter.filter(null, c);

        Assert.assertEquals(JsonUtf8ResponseFilter.APPLICATION_JSON_UTF8_TYPE, r2.getMediaType());
    }

    @Test
    public void testUnchanged()
    {
        final JsonUtf8ResponseFilter filter = new JsonUtf8ResponseFilter();

        final ContainerResponse c = new ContainerResponse(null, null, null);
        c.setResponse(Response.ok().type(MediaType.APPLICATION_XML_TYPE).build());

        final ContainerResponse r2 = filter.filter(null, c);

        Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r2.getMediaType());
    }

    @Test
    public void testAlready()
    {
        final JsonUtf8ResponseFilter filter = new JsonUtf8ResponseFilter();

        final ContainerResponse c = new ContainerResponse(null, null, null);
        c.setResponse(Response.ok().type(JsonUtf8ResponseFilter.APPLICATION_JSON_UTF8_TYPE).build());

        final ContainerResponse r2 = filter.filter(null, c);

        Assert.assertEquals(JsonUtf8ResponseFilter.APPLICATION_JSON_UTF8_TYPE, r2.getMediaType());
    }

    @Test
    public void testUnset()
    {
        final JsonUtf8ResponseFilter filter = new JsonUtf8ResponseFilter();

        final ContainerResponse c = new ContainerResponse(null, null, null);
        c.setResponse(Response.ok().build());

        final ContainerResponse r2 = filter.filter(null, c);

        Assert.assertNull(r2.getMediaType());
    }

    @Test
    public void testHowAboutAWildcard()
    {
        final JsonUtf8ResponseFilter filter = new JsonUtf8ResponseFilter();

        final ContainerResponse c = new ContainerResponse(null, null, null);
        c.setResponse(Response.ok().type(new MediaType("*", "json")).build());

        final ContainerResponse r2 = filter.filter(null, c);

        Assert.assertEquals(JsonUtf8ResponseFilter.APPLICATION_JSON_UTF8_TYPE, r2.getMediaType());
    }

    @Test
    public void testHowAboutABadMediatype()
    {
        final JsonUtf8ResponseFilter filter = new JsonUtf8ResponseFilter();

        final ContainerResponse c = new ContainerResponse(null, null, null);
        c.setResponse(Response.ok().type(new MediaType("text", "json")).build());

        final ContainerResponse r2 = filter.filter(null, c);

        Assert.assertEquals(JsonUtf8ResponseFilter.APPLICATION_JSON_UTF8_TYPE, r2.getMediaType());
    }

    @Test
    public void testDontTouchTheText()
    {
        final JsonUtf8ResponseFilter filter = new JsonUtf8ResponseFilter();

        final ContainerResponse c = new ContainerResponse(null, null, null);
        c.setResponse(Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build());

        final ContainerResponse r2 = filter.filter(null, c);

        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, r2.getMediaType());
    }
}
