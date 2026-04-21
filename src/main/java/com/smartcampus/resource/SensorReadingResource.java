package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * SensorReadingResource — Part 4 Sub-Resource.
 *
 * Handles: GET /api/v1/sensors/{sensorId}/readings
 *          POST /api/v1/sensors/{sensorId}/readings
 */ 
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the complete historical reading log for this sensor.
     * Returns 404 if the sensor does not exist.
     */
    @GET
    public Response getReadings() {
        //check if sensor exists
        if (!store.getSensors().containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor '" + sensorId + "' not found."))
                    .build();
        }
        //if yes, give the reading of it, if not return an empty list 
        List<SensorReading> history = store.getReadings()
                .getOrDefault(sensorId, Collections.emptyList());

        //Response structure
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensorId", sensorId);
        response.put("totalReadings", history.size());
        response.put("readings", history);
        return Response.ok(response).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to this sensor's history.
     *
     * Throws SensorUnavailableException (HTTP 403) if the sensor status is "MAINTENANCE".
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);
        //check if sensor exists
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor '" + sensorId + "' not found."))
                    .build();
        }

        // State constraint: sensors under maintenance cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently under MAINTENANCE and cannot " +
                    "accept new readings. Please wait until the sensor is restored to ACTIVE status."
            );
        }
        
        //"Request body with a 'value' field is required."
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Request body with a 'value' field is required."))
                    .build();
        }

        // Auto-generate reading id and timestamp if not provided by the client
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Find the list of readings for this sensor. If it doesn’t exist, creates it. Then add the new reading to it
        store.getReadings()
             .computeIfAbsent(sensorId, k -> new ArrayList<>())
             .add(reading);

        // SIDE EFFECT: Update the parent sensor's currentValue for data consistency
        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Reading recorded successfully.");
        response.put("sensorId", sensorId);
        response.put("updatedCurrentValue", sensor.getCurrentValue());
        response.put("reading", reading);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}  