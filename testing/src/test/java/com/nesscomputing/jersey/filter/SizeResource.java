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
package com.nesscomputing.jersey.filter;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@BodySizeLimit(1024)
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SizeResource {
    @POST
    @Path("/post_inherit")
    public int doPost(byte[] data) {
        return data.length;
    }

    @POST
    @Path("/post_small")
    @BodySizeLimit(128)
    public int doPostSmall(byte[] data) {
        return data.length;
    }

    @POST
    @Path("/post_big")
    @BodySizeLimit(4096)
    public int doPostLarge(byte[] data) {
        return data.length;
    }

    @PUT
    @Path("/put")
    public int doPut(byte[] data) {
        return data.length;
    }

    @POST
    @Path("/post_configured")
    public int doPostConfigured(byte[] data) {
        return data.length;
    }
}
