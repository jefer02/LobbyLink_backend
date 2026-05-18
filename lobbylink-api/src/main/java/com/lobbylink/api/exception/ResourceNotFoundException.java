package com.lobbylink.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource is not found in the database.
 * Maps to HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}
