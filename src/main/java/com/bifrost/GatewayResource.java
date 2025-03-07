package com.bifrost;

import com.bifrost.forwarding.RouteForwarder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/gateway")
public class GatewayResource {
	@GET
	@Path("/{service}/{remaining: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response forwardGet(@PathParam("service") String service, @PathParam("remaining") String remainingPath) {
		return forwardRequest(service, remainingPath, "GET", null);
	}

	@GET
	@Path("/{service}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response forwardGet(@PathParam("service") String service) {
		return forwardRequest(service, "", "GET", null);
	}


	@POST
	@Path("/{service}/{remaining: .*}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forwardPost(
			@PathParam("service") String service,
			@PathParam("remaining") String remainingPath,
			String requestBody) {
		return forwardRequest(service, remainingPath, "POST", requestBody);
	}

	@POST
	@Path("/{service}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forwardPost(
			@PathParam("service") String service,
			String requestBody) {
		return forwardRequest(service, "", "POST", requestBody);
	}



	private Response forwardRequest(String service, String remainingPath, String method, String requestBody) {
		return RouteForwarder.forwardRequest(service, remainingPath, method, requestBody);
	}



}

