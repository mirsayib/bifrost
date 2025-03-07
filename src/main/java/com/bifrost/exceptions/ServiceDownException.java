package com.bifrost.exceptions;

public class ServiceDownException extends RuntimeException {
	public ServiceDownException(String serviceName, String message) {
		super("Service is down: " + serviceName + ": " + message);
	}
}
