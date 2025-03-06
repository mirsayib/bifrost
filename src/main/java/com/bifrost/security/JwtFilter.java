package com.bifrost.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Objects;

@Provider
@PreMatching
public class JwtFilter implements ContainerRequestFilter {
	private static final String AUTH_HEADER = "Authorization";
	@Override
	public void filter(ContainerRequestContext requestContext) {
		String authHeader = requestContext.getHeaders().getFirst(AUTH_HEADER);

		if(Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
			requestContext
				.abortWith(
					Response.status(Response.Status.UNAUTHORIZED)
						.entity("Unauthorized")
						.build()
				);
			return;
		}

		String token = authHeader.substring(7);

		if(!JwtUtil.validateJwt(token)) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity("Invalid or expired token").build());
		}

	}
}
