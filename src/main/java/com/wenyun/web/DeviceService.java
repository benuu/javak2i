package com.wenyun.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/ps")
public class DeviceService {
	
	@GET
	@Path("/test")
	public Response psTest() {
 
		String output = "This is pump service test function";
 
		return Response.status(200).entity(output).build();
 
	}

}
