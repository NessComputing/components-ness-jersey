package com.nesscomputing.jersey;


import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * Enforces charset=utf8 on our json responses. Strictly spoken this is not necessary because application/json is inherently UTF-8 according to the
 * RFC, but it seems that there are client libraries out there that do not know that. So let's be sure.
 *
 */
public class JsonUtf8ResponseFilter implements ContainerResponseFilter
{
    public static final MediaType APPLICATION_JSON_UTF8_TYPE = new MediaType("application", "json", Collections.singletonMap("charset", "utf-8"));
    public static final MediaType TEXT_JSON = new MediaType("text", "json");

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getMediaType()) || TEXT_JSON.isCompatible(response.getMediaType())) {
            response.setResponse(Response.fromResponse(response.getResponse()).type(APPLICATION_JSON_UTF8_TYPE).build());
        }

        return response;
    }
}
