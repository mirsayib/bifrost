package com.bifrost.utils;

import com.bifrost.exceptions.ServiceDownException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class ExceptionMapperUtil implements ExceptionMapper<Exception> {
	@Override
	public Response toResponse(Exception exception) {
		Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

		if(exception instanceof ServiceDownException){
			status = Response.Status.SERVICE_UNAVAILABLE;
		}

		String jsonResponse = String.format(
				"{\"error\": \"%s\", \"message\": \"%s\"}",
				exception.getClass().getSimpleName(), exception.getMessage()
		);
		return Response.status(status)
				.entity(jsonResponse)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
}
