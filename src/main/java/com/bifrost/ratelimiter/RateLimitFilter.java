package com.bifrost.ratelimiter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.net.InetAddress;

@Provider
@PreMatching
public class RateLimitFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) {
		String clientIP = getClientIp();

		if(!RedisRateLimiter.isAllowed(clientIP)){
			requestContext.abortWith(
				Response.status(Response.Status.TOO_MANY_REQUESTS)
					.entity("Too many requests!")
					.build()
			);
		}

	}

	private String getClientIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			return "unknown";
		}
	}
}
