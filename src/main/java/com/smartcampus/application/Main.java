package com.smartcampus.application;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.resource.SensorReadingResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";  // <-- changed

    public static void main(String[] args) throws Exception {
        ResourceConfig config = new ResourceConfig();

        // Resources
        config.register(DiscoveryResource.class);
        config.register(RoomResource.class);
        config.register(SensorResource.class);
        // DO NOT register SensorReadingResource - it's a sub-resource

        // Exception Mappers
        config.register(RoomNotEmptyExceptionMapper.class);
        config.register(LinkedResourceNotFoundExceptionMapper.class);
        config.register(SensorUnavailableExceptionMapper.class);
        config.register(GlobalExceptionMapper.class);

        // Filters & Features
        config.register(LoggingFilter.class);
        config.register(JacksonFeature.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        LOGGER.info("==============================================");
        LOGGER.info(" Smart Campus API started successfully!");
        LOGGER.info(" Rooms    : http://localhost:8080/api/v1/rooms");
        LOGGER.info(" Sensors  : http://localhost:8080/api/v1/sensors");
        LOGGER.info("==============================================");
        LOGGER.info(" Press CTRL+C to stop the server.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            server.shutdownNow();
        }));

        Thread.currentThread().join();
    }
}

// This class is the entry point of the Smart Campus API. It configures and starts an embedded Grizzly HTTP server, registers all JAX-RS resources using Jersey’s ResourceConfig, enables JSON support via Jackson, and keeps the server running until manually terminated. It also includes a shutdown hook to ensure graceful server termination.

// The Main class is the entry point of the application. It defines the base URL of the server and starts an embedded Grizzly HTTP server.
//
// Jersey is used as the JAX-RS implementation, which allows us to build RESTful APIs in Java. In this class, we configure Jersey using ResourceConfig, which scans all packages under com.smartcampus to automatically detect resources, filters, and exception mappers.
//
// We also register JacksonFeature, which enables automatic conversion between Java objects and JSON format, allowing the API to send and receive JSON data.
//
//        Finally, the server is started and kept running, and logging is used to display important startup and shutdown information.