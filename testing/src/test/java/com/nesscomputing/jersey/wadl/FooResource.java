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
package com.nesscomputing.jersey.wadl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

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
