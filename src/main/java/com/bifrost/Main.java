package com.bifrost;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class Main {
	private static final String BASE_URI = "http://localhost:8080/api/";
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
		ResourceConfig config = new ResourceConfig()
				.packages("com.bifrost")
				.register(com.bifrost.security.JwtFilter.class)
				.register(com.bifrost.ratelimiter.RateLimitFilter.class)
				.register(com.bifrost.utils.ExceptionMapperUtil.class);

		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

		logger.info("Bifrost API Gateway started at {}", BASE_URI);

		Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
	}
}
