package com.wenyun.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/pss")
public class PumpStationService {
	
	@GET
	@Path("/test")
	public Response psTest() {
 
		String output = "This is pump station service test function";
 
		return Response.status(200).entity(output).build();
 
	}
	
	@GET
	@Path("/{psname}")
	public Response psTest(@PathParam("psname") String psname) {
 
		String output = "This is pump station service test function";
 
		return Response.status(200).entity(output).build();
 
	}

}
