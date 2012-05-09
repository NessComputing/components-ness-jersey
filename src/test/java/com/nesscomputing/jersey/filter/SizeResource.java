package com.nesscomputing.jersey.filter;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.nesscomputing.jersey.filter.BodySizeLimit;

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
