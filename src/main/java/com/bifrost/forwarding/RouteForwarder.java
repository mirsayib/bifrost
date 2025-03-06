package com.bifrost.forwarding;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class RouteForwarder {

	private RouteForwarder() {}

	private static final Client client = ClientBuilder.newClient();

	public static Response forwardRequest(String backendUrl, String method, String requestBody) {
		try {
			WebTarget target = client.target(backendUrl);
			Invocation.Builder requestBuilder = target.request(MediaType.APPLICATION_JSON);

			return switch (method) {
				case "POST" -> requestBuilder.post(Entity.json(requestBody));
				case "PUT" -> requestBuilder.put(Entity.json(requestBody));
				case "DELETE" -> requestBuilder.delete();
				default -> // GET
						requestBuilder.get();
			};
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_GATEWAY)
					.entity("❌ Failed to reach backend service: " + backendUrl)
					.build();
		}
	}
}
