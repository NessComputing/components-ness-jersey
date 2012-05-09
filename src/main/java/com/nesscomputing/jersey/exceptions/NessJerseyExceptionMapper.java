package com.nesscomputing.jersey.exceptions;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.MDC;

import com.google.common.collect.ImmutableMap;

/**
 * A generic base class to map an exception to a Ness response. This must be implemented by a concrete
 * class because the Jersey IoC container requires concrete classes and could not deal with an inner class that
 * implements this. An example would be
 *
 * <pre>   @Provider
   public static final class IllegalArgumentExceptionMapper extends NessJerseyExceptionMapper<IllegalArgumentException>
   {
       public IllegalArgumentExceptionMapper()
       {
           super(Status.BAD_REQUEST, IllegalArgumentException.class);
       }
   }
   </pre>
 */
public abstract class NessJerseyExceptionMapper<U extends Throwable> implements ExceptionMapper<U>
{
    private final Status statusCode;

    protected NessJerseyExceptionMapper(final Status statusCode)
    {
        this.statusCode = statusCode;
    }

    @Override
    public Response toResponse(final U exception)
    {
        final Map<String, String> response = ImmutableMap.of("code", statusCode.toString(),
                                                             "trace", ObjectUtils.toString(MDC.get("track")),
                                                             "message", ObjectUtils.toString(exception.getMessage(), "(no message)"));

        return Response.status(statusCode)
        .entity(response)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
    }
}
