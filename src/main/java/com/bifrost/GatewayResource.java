package com.bifrost;

import com.bifrost.config.RouteConfig;
import com.bifrost.ratelimiter.RedisRateLimiter;
import com.bifrost.forwarding.RouteForwarder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.util.Objects;

@Path("/gateway")
public class GatewayResource {
	private static final Logger logger = LoggerFactory.getLogger(GatewayResource.class);
	String clientIp = getClientIp();

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
		String backendUrl = RouteConfig.getBackendUrl(service);

		if(!RedisRateLimiter.isAllowed(clientIp)){
			return Response.status(429).entity("Too Many Requests!").build();
		}

		if (Objects.isNull(backendUrl)) {
			return Response.status(404).entity("❌ Service not found: " + service).build();
		}
		URI finalUrl = buildFinalUrl(backendUrl, remainingPath);

		logger.info("Forwarding request to {}", finalUrl);
		return RouteForwarder.forwardRequest(finalUrl.toString(), method, requestBody);
	}

	private URI buildFinalUrl(String backendUrl, String remainingPath) {
		URI baseUri = URI.create(backendUrl);
		return (Objects.isNull(remainingPath) || remainingPath.isEmpty()) ? baseUri : baseUri.resolve(remainingPath);
	}

	private String getClientIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			return "unknown";
		}
	}
}

