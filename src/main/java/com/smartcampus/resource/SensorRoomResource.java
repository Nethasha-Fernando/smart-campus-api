package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.NotFoundException;  
import java.util.*;

/**
 * Room Resource — Part 2.
 * Follows the same return-style as DiscoveryResource for GET methods.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final DataStore store = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Fetches all rooms from DataStore, and returns the total number of rooms and the full room list.
     */
    @GET
    public Map<String, Object> getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", roomList.size());
        response.put("rooms", roomList);
        return response;
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with the created room.
     */
    @POST
    public Response createRoom(Room room) {
        // check if ID exists, if not reject request (400 Bad Request)
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Room 'id' is required."))
                    .build();
        }
        // Check if the room has a name, if not reject it
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Room 'name' is required."))
                    .build();
        }
        // if the room ID already exists reject with 409 Conflict
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody("A room with id '" + room.getId() + "' already exists."))
                    .build();
        }
        // if room has no sensor list creates one
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        // save the room
        store.getRooms().put(room.getId(), room);

        // Return the response 
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room created successfully.");
        response.put("room", room);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns a single room object by the ID and return it or throws NotFoundException (maps to 404).
     */
    
    @GET
    @Path("/{roomId}")
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            throw new NotFoundException("Room with id '" + roomId + "' not found.");
        }
        return room;
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room if it has no sensors. Throws RoomNotEmptyException if sensors present.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        //get the room by ID, if it doenst exist throw an error
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            throw new NotFoundException("Room with id '" + roomId + "' not found.");
        }
        //check if sensors are available in a room if yes,do not delete the room.
        if (!room.getSensorIds().isEmpty()) {
        throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted because it still has active sensors assigned to it.",
                new java.util.ArrayList<>(room.getSensorIds())
        );
    }
        //Removes the room
        store.getRooms().remove(roomId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room '" + roomId + "' has been successfully decommissioned.");
        response.put("deletedRoomId", roomId);
        return Response.ok(response).build();
    }
    // Helper method that craetes a standard error response for your API
    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}