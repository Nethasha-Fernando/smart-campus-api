package com.smartcampus.application;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.SensorRoomResource;
import com.smartcampus.resource.SensorResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Main (Application Entry Point)
 *
 * Responsible for configuring and starting the Smart Campus REST API.
 * Uses Jersey (JAX-RS) with an embedded Grizzly HTTP server.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    // Base URI where the API will be available
    private static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";

    public static void main(String[] args) throws Exception {

        // Jersey configuration object used to register all components
        ResourceConfig config = new ResourceConfig();

        // ================================
        // Register API Resources (Endpoints)
        // ================================
        config.register(DiscoveryResource.class);
        config.register(SensorRoomResource.class);
        config.register(SensorResource.class);

        // ================================
        // Register Exception Mappers
        // Converts exceptions into HTTP responses
        // ================================
        config.register(RoomNotEmptyExceptionMapper.class);
        config.register(LinkedResourceNotFoundExceptionMapper.class);
        config.register(SensorUnavailableExceptionMapper.class);
        config.register(GlobalExceptionMapper.class);

        // ================================
        // Register Filters & Features
        // ================================
        config.register(LoggingFilter.class);   // Logs all requests & responses
        config.register(JacksonFeature.class); // Enables JSON serialization/deserialization

        // Create and start embedded Grizzly server
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), config
        );

        // ================================
        // Startup Logs
        // ================================
        LOGGER.info("==============================================");
        LOGGER.info(" Smart Campus API started successfully!");
        LOGGER.info(" Rooms    : http://localhost:8080/api/v1/rooms");
        LOGGER.info(" Sensors  : http://localhost:8080/api/v1/sensors");
        LOGGER.info("==============================================");
        LOGGER.info(" Press CTRL+C to stop the server.");

        // Graceful shutdown hook (runs when app is terminated)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            server.shutdownNow();
        }));

        // Keeps server running indefinitely
        Thread.currentThread().join();
    }
}