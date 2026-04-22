package com.smartcampus.exception;

import java.util.List;

/**
 * Thrown when a room deletion is attempted but the room still has sensors assigned.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {

    private final List<String> activeSensorIds;

    public RoomNotEmptyException(String message, List<String> activeSensorIds) {
        super(message);
        this.activeSensorIds = activeSensorIds;
    }

    public List<String> getActiveSensorIds() {
        return activeSensorIds;
    }
}