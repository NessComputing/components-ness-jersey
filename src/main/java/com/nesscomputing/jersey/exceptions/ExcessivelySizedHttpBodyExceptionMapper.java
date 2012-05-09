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
