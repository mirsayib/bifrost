package com.bifrost.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class RouteConfig {

	private RouteConfig() {}
	private static final String CONFIG_FILE = "/routes.json"; // Load from classpath
	private static Map<String, String> routes;

	static {
		loadRoutes();
	}

	private static void loadRoutes() {
		try (InputStreamReader reader = new InputStreamReader(
				Objects.requireNonNull(RouteConfig.class.getResourceAsStream(CONFIG_FILE)))) {
			Type mapType = new TypeToken<Map<String, String>>() {}.getType();
			routes = new Gson().fromJson(reader, mapType);
			System.out.println("✅ Routes loaded: " + routes);
		} catch (Exception e) {
			System.err.println("❌ Failed to load routes: " + e.getMessage());
			routes = Collections.emptyMap(); // Fallback to empty routes
		}
	}

	public static String getBackendUrl(String service) {
		return routes.getOrDefault(service, null);
	}
}
