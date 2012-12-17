package com.nesscomputing.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.inject.Singleton;

@Provider
@Singleton
class ResponseMapper implements ExceptionMapper<NessApiException>
{
    @Override
    public Response toResponse(NessApiException exception)
    {
        return Response.status(exception.getStatus())
                .type(NessApiException.MEDIA_TYPE)
                .entity(new NessApiExceptionEntity(exception.getStatus(), exception.getJsonRepresentation()))
                .build();
    }
}
