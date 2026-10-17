package com.solaria.persistence.exception;

import org.springframework.http.HttpStatus;

public class ServiceTransitionConflictException extends BusinessException {

    public ServiceTransitionConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "INVALID_SERVICE_TRANSITION");
    }
}
