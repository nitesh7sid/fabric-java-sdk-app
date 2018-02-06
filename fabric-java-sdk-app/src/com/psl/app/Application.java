package com.psl.app;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import com.psl.app.RequestData;
import com.psl.fabric.util.CAUtility;

@Path("/api")
public class Application {

	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUser(RequestData request) {

		System.out.println("Input"+request);
		JSONObject response = new JSONObject();
		String enrollSecret = "";
		try {
			enrollSecret = CAUtility.registerUser(request.getUsername(), request.getOrg(), request.getUseraffiliation());
			
		} catch (Exception e) {
			e.printStackTrace();
			response.put("message", e.getMessage());
			return Response.status(500).entity(response).build();
		}
		
		response.put("message", enrollSecret);
		return Response.status(201).entity(response).build();
		
	}
	
	@POST
	@Path("/enroll")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enrollUser(@QueryParam("userName") String userName,
			@QueryParam("enrollSecret") String enrollSecret,@QueryParam("userOrg") String userOrg) {

		return Response.status(200).build();
		
	}

}
