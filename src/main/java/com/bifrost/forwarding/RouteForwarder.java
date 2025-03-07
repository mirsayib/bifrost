package com.bifrost.forwarding;

import com.bifrost.config.ConfigLoader;
import com.bifrost.exceptions.ServiceDownException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

public class RouteForwarder {

	private RouteForwarder() {}

	private static final Logger logger = LoggerFactory.getLogger(RouteForwarder.class);
	private static final Client client = ClientBuilder.newClient();


	public static Response forwardRequest(String service, String remainingPath, String method, String requestBody){

		String backendUrl = ConfigLoader.getBackendUrl(service);
		if (Objects.isNull(backendUrl)) {
			return Response.status(404).entity("Service not found: " + service).build();
		}
		URI finalUrl = buildFinalUrl(backendUrl, remainingPath);

		logger.info("Forwarding request to {}", finalUrl);
		// Get the correct Circuit Breaker for this service
		CircuitBreaker circuitBreaker = ConfigLoader.getCircuitBreaker(service);

		// Log circuit breaker state changes per service
		circuitBreaker.getEventPublisher().onStateTransition(
				event -> logger.info(" Circuit breaker for '{}' changed state: {}", service, event.getStateTransition())
		);

		Supplier<Response> requestSupplier = () -> sendHttpRequest(finalUrl.toString(), method, requestBody);
		return circuitBreaker.executeSupplier(requestSupplier);
	}


	private static Response sendHttpRequest(String backendUrl, String method, String requestBody) {
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
			throw new ServiceDownException(backendUrl, e.getMessage());
		}
	}

	private static URI buildFinalUrl(String backendUrl, String remainingPath) {
		URI baseUri = URI.create(backendUrl);
		return (Objects.isNull(remainingPath) || remainingPath.isEmpty()) ? baseUri : baseUri.resolve(remainingPath);
	}
}
