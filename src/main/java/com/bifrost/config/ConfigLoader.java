package com.bifrost.config;

import com.bifrost.exceptions.ServiceDownException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigLoader {
	public static final String FAILURE_RATE_THRESHOLD = "failureRateThreshold";
	public static final String WAIT_DURATION_IN_OPEN_STATE = "waitDurationInOpenState";
	public static final String SLIDING_WINDOW_SIZE = "slidingWindowSize";
	public static final String MINIMUM_NUMBER_OF_CALLS = "minimumNumberOfCalls";
	private static final String CONFIG_FILE = "/config.json"; // Unified Config File
	public static final String ROUTES = "routes";
	public static final String CIRCUIT_BREAKERS = "circuitBreakers";


	
	private static Map<String, String> routesMap;
	private static Map<String, Map<String, Double>> circuitBreakerConfigs;
	private static final Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	private ConfigLoader() {}
	// Load configuration on startup
	static {
		loadConfig();
	}

	/**  Load Routes & Circuit Breaker Configs from JSON **/
	private static void loadConfig() {
		try (InputStreamReader reader = new InputStreamReader(
				Objects.requireNonNull(ConfigLoader.class.getResourceAsStream(CONFIG_FILE)))) {

			Gson gson = new Gson();
			Type configType = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> config = gson.fromJson(reader, configType);

			//  Load routes
			loadRoutes(config, gson);

			//  Load Circuit Breaker Configurations for Each Service
			loadCircuitBreakerConfig(config, gson);

			//  Initialize Circuit Breakers for each service
			initCircuitBreakers();

			logger.info(" Config Loaded: Routes & Circuit Breaker settings");

		} catch (Exception e) {
			logger.error(e.getMessage());
			routesMap = Collections.emptyMap();
			circuitBreakerConfigs = Collections.emptyMap();
		}
	}

	private static void initCircuitBreakers() {
		for (String service : circuitBreakerConfigs.keySet()) {
			circuitBreakerMap.put(service, CircuitBreaker.of(service, createCircuitBreakerConfig(service)));
		}
	}

	private static void loadCircuitBreakerConfig(Map<String, Object> config, Gson gson) {
		if (config.containsKey(CIRCUIT_BREAKERS)) {
			String cbJson = gson.toJson(config.get(CIRCUIT_BREAKERS));
			circuitBreakerConfigs
					= gson.fromJson(cbJson, new TypeToken<Map<String, Map<String, Double>>>() {}.getType());
		} else {
			circuitBreakerConfigs = Collections.emptyMap();
		}
	}

	private static void loadRoutes(Map<String, Object> config, Gson gson) {
		if (config.containsKey(ROUTES)) {
			String routesJson = gson.toJson(config.get(ROUTES));
			routesMap = gson.fromJson(routesJson, new TypeToken<Map<String, String>>() {}.getType());
		} else {
			routesMap = Collections.emptyMap();
		}
	}

	/**  Get Backend URL **/
	public static String getBackendUrl(String service) {
		return routesMap.getOrDefault(service, null);
	}

	/**  Get Circuit Breaker for a Specific Service **/
	public static CircuitBreaker getCircuitBreaker(String serviceName) {
		CircuitBreakerConfig circuitBreakerConfig = createCircuitBreakerConfig(serviceName);
		CircuitBreakerRegistry circuitBreakerRegistry = createCircuitBreakerRegistry(circuitBreakerConfig);
		return circuitBreakerMap.computeIfAbsent(serviceName, circuitBreakerRegistry::circuitBreaker);
	}

	
	/**  Create Circuit Breaker Config for a Specific Service **/
	private static CircuitBreakerConfig createCircuitBreakerConfig(String serviceName) {
		Map<String, Double> cbConfig = circuitBreakerConfigs.getOrDefault(serviceName, Collections.emptyMap());

		return CircuitBreakerConfig.custom()
				.failureRateThreshold(cbConfig.getOrDefault(FAILURE_RATE_THRESHOLD, 50.0).floatValue())
				.waitDurationInOpenState(
						Duration.ofSeconds(
								cbConfig.getOrDefault(WAIT_DURATION_IN_OPEN_STATE, 10.0).longValue()
						)
				)
				.slidingWindowSize(cbConfig.getOrDefault(SLIDING_WINDOW_SIZE, 10.0).intValue())
				.minimumNumberOfCalls(cbConfig.getOrDefault(MINIMUM_NUMBER_OF_CALLS, 5.0).intValue())
				.recordException(ServiceDownException.class::isInstance)
				.build();
	}

	/** Create Circuit Breaker Registry **/
	private static CircuitBreakerRegistry createCircuitBreakerRegistry(CircuitBreakerConfig circuitBreakerConfig) {
		return CircuitBreakerRegistry.of(circuitBreakerConfig);
	}

}
