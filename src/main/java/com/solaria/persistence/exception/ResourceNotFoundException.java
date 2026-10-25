package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public ResourceNotFoundException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND", properties);
    }
}
