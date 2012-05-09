package com.nesscomputing.jersey.exceptions;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.MDC;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

@Provider
public class JsonMessageReaderMapper extends NessJerseyExceptionMapper<JsonParseException> {

    @Inject
    public JsonMessageReaderMapper() {
        super(Status.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Response toResponse(JsonParseException exception) {
        for (StackTraceElement e : exception.getStackTrace()) {
            if (JacksonJsonProvider.class.getName().equals(e.getClassName())) {
                final Map<String, String> response = ImmutableMap.of(
                        "code", "400",
                        "trace", ObjectUtils.toString(MDC.get("track")),
                        "message", ObjectUtils.toString(exception.getMessage(), "(no message)"));

                return Response.status(400)
                        .entity(response)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
        }

        return super.toResponse(exception);
    }
}
