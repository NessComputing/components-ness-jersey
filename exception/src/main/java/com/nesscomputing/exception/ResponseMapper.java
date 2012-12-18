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
