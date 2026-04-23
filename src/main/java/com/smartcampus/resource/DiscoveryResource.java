package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Resource — Part 1, Task 2.
 *
 * Provides API metadata at the root entry point GET /api/v1.
 * This implements a basic HATEOAS-style discovery document so API consumers
 * can navigate the entire API from a single well-known URL.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discover() {
        Map<String, Object> discovery = new LinkedHashMap<>();

        // Versioning info
        discovery.put("api", "Smart Campus Sensor & Room Management API");
        discovery.put("version", "1.0.0");
        discovery.put("status", "operational");
        discovery.put("description",
                "A RESTful API for managing campus rooms and IoT sensors " +
                        "built with JAX-RS (Jersey) deployed on Apache Tomcat.");

        // Administrative contact
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("Team", "University of Westminster - Smart Campus Initiative");
        contact.put("Module", "5COSC022W Client-Server Architectures");
        contact.put("Email", "smartcampus@westminster.ac.uk");
        discovery.put("contact", contact);

        // Primary resource links (HATEOAS navigation map)
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("sensor_by_type", "/api/v1/sensors?type={type}");
        links.put("sensor_readings", "/api/v1/sensors/{sensorId}/readings");
        discovery.put("_links", links);

        return discovery;
    }
}

