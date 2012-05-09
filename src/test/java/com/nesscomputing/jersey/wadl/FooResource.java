package com.nesscomputing.jersey.wadl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import com.nesscomputing.jersey.wadl.RequiresAuthentication;
import com.nesscomputing.jersey.wadl.RequiresAuthentication.AuthenticationType;

@Path("/foo")
public class FooResource {

	@POST
	@RequiresAuthentication
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
	public String echo(String content) {
		return content;
	}

	@Path("/{what}")
	@GET
	@RequiresAuthentication(AuthenticationType.NONE)
	public String echoGet(@PathParam("what") String what) {
		return what;
	}

	@Path("/xxx")
	@GET
	public String getXXX() {
	    return "xxx";
	}
}
