package com.nesscomputing.jersey.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

/**
 * Map the {@link ExcessivelySizedHttpBodyException} to a HTTP 400 Bad Request
 */
@Provider
public class ExcessivelySizedHttpBodyExceptionMapper extends NessJerseyExceptionMapper<ExcessivelySizedHttpBodyException> {

    public ExcessivelySizedHttpBodyExceptionMapper() {
        super(Status.BAD_REQUEST);
    }

    @Override
    public Response toResponse(ExcessivelySizedHttpBodyException exception) {
        return Response.status(Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity(exception.getMessage())
                .build();
    }
}
