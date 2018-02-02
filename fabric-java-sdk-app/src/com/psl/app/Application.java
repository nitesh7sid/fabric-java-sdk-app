package com.psl.app;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/users")
public class Application {

	@GET
	@Path("/register")
	public Response registerUser(@QueryParam("userName") String userName,
			@QueryParam("userOrg") String userOrg) {

		return Response.status(200).build();
		
	}
	
	@POST
	@Path("/enroll")
	public Response enrollUser(@QueryParam("userName") String userName,
			@QueryParam("enrollSecret") String enrollSecret,@QueryParam("userOrg") String userOrg) {

		return Response.status(200).build();
		
	}

}
