package com.smartcampus.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.SensorRoomResource;
import com.smartcampus.resource.SensorResource;

/**
 * SmartCampusApplication
 *
 * JAX-RS configuration class that defines the base API path
 * and explicitly registers all components used by the application.
 *
 * The @ApplicationPath("/api/v1") sets the base URI for all endpoints.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    /**
     * Registers all classes that should be loaded by Jersey at runtime.
     * Includes resources (endpoints), exception mappers, and filters.
     */
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // ================================
        // Resources (API Endpoints)
        // ================================
        classes.add(DiscoveryResource.class);
        classes.add(SensorRoomResource.class);
        classes.add(SensorResource.class);

        // ================================
        // Exception Mappers
        // Convert exceptions into proper HTTP responses
        // ================================
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // ================================
        // Filters (Cross-cutting concerns)
        // ================================
        classes.add(LoggingFilter.class); // Logs all requests and responses

        return classes;
    }
}