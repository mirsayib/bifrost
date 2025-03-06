package com.bifrost;

import com.bifrost.ratelimiter.RedisRateLimiter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.InetAddress;

@Path("/gateway")
public class GatewayResource {
	String clientIp = getClientIp();
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response rateLimitedEndpoint() {
		if(!RedisRateLimiter.isAllowed(clientIp)){
			return Response.status(429).entity("Too Many Requests!").build();
		}

		return Response.ok("Bifrost API Gateway is running, IP: " + clientIp).build();
	}

	private String getClientIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			return "unknown";
		}
	}
}

