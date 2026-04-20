package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sensor Resource — Part 3.
 * Handles all API operations related to sensors.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance(); // Access shared in-memory data

    /**
     * GET /api/v1/sensors
     * Optional filter: ?type=CO2
     * Returns all sensors or filters by type.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {

        // Get all sensors from the data store
        List<Sensor> sensorList = new ArrayList<>(store.getSensors().values());

        // Filter sensors by type if query parameter is provided
        if (type != null && !type.isBlank()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", sensorList.size());

        // Include filter info if applied
        if (type != null && !type.isBlank()) {
            response.put("filterApplied", Map.of("type", type));
        }

        response.put("sensors", sensorList);

        return Response.ok(response).build();
    }

    /**
     * POST /api/v1/sensors
     * Creates a new sensor and links it to a room.
     */
    @POST
    public Response createSensor(Sensor sensor) {

        // Validate required fields
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Sensor 'id' is required."))
                    .build();
        }

        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Sensor 'type' is required."))
                    .build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Sensor 'roomId' is required."))
                    .build();
        }

        // Check if sensor ID already exists
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody("A sensor with id '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // Ensure the referenced room exists
        if (!store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "The roomId '" + sensor.getRoomId() + "' does not exist."
            );
        }

        // Set default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        // Save sensor
        store.getSensors().put(sensor.getId(), sensor);

        // Link sensor to room
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Create empty readings list for this sensor
        store.getReadings().put(sensor.getId(), new ArrayList<>());

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor registered successfully.");
        response.put("sensor", sensor);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {

        Sensor sensor = store.getSensors().get(sensorId);

        // Return 404 if sensor not found
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor with id '" + sensorId + "' not found."))
                    .build();
        }

        return Response.ok(sensor).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Deletes a sensor and removes it from its room.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {

        Sensor sensor = store.getSensors().get(sensorId);

        // Return 404 if sensor not found
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor with id '" + sensorId + "' not found."))
                    .build();
        }

        // Remove sensor reference from the parent room
        if (sensor.getRoomId() != null && store.getRooms().containsKey(sensor.getRoomId())) {
            store.getRooms().get(sensor.getRoomId()).getSensorIds().remove(sensorId);
        }

        // Delete sensor and its readings
        store.getSensors().remove(sensorId);
        store.getReadings().remove(sensorId);

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor '" + sensorId + "' has been successfully removed.");
        response.put("deletedSensorId", sensorId);

        return Response.ok(response).build();
    }

    /**
     * Sub-resource locator for sensor readings.
     * Routes requests to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    /**
     * Helper method to create consistent error responses.
     */
    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}