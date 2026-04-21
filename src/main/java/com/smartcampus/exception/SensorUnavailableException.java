package com.smartcampus.exception;

/**
 * Thrown when a reading POST is attempted on a sensor that is not in ACTIVE status.
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
