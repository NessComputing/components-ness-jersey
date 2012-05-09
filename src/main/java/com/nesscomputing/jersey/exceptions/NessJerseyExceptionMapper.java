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
