package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    private static final Logger LOGGER =
            Logger.getLogger(LinkedResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        LOGGER.warning("LinkedResourceNotFoundException: " + exception.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 422);
        body.put("error", "Unprocessable Entity");
        body.put("code", "LINKED_RESOURCE_NOT_FOUND");
        body.put("message", exception.getMessage());
        body.put("hint", "Ensure the referenced resource (e.g., roomId) exists before creating a dependent resource.");
        body.put("timestamp", System.currentTimeMillis());

        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
