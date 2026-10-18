package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BusinessException {

    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }

    public UnauthorizedAccessException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED", properties);
    }
}
