package com.smartcampus.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;

 /**
 * JAX-RS Application configuration class.
 *
 * The @ApplicationPath("/api/v1") defines the base URI for all API endpoints.
 * Every resource in this application will be accessible under this path.
 *
 * This class extends the JAX-RS Application class and overrides the getClasses() method
 * to explicitly register all components used by the API.
 *
 * These include resource classes (which define endpoints), exception mappers
  * (which handle errors and return proper HTTP responses), and filters (such as logging).
  * This ensures that Jersey knows exactly which classes to use when handling incoming requests.
 */

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {  // This method returns a list of all components Jersey should load:
        Set<Class<?>> classes = new HashSet<>();

        // Resources (API endpoints and registered here)
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception Mappers (Instead of crashing your API, you map errors to HTTP responses like 404 likewisr)
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // Filters (automatically runs for every req logs, incoming request and outgoing response)
        classes.add(LoggingFilter.class);

        return classes;  //Load everything in this set when starting the server.”
    }
}
