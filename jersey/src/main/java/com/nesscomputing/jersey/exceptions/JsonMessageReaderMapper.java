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
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.ObjectUtils;
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
