package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * LoggingFilter
 *
 * A global filter that logs all incoming HTTP requests and outgoing responses.
 * Demonstrates a cross-cutting concern handled centrally instead of inside resource classes.
 */
@Provider 
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Runs BEFORE the request reaches the resource method.
     * Logs HTTP method and request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format(
                "[REQUEST]  --> %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()
        ));
    }

    /**
     * Runs AFTER the resource method has produced a response.
     * Logs HTTP status code along with method and URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format(
                "[RESPONSE] <-- %d %s | %s %s",
                responseContext.getStatus(),
                responseContext.getStatusInfo().getReasonPhrase(),
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()
        ));
    }
}