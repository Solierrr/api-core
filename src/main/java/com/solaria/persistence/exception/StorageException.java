package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class StorageException extends BusinessException {

    public StorageException(String message) {
        super(message, HttpStatus.BAD_GATEWAY, "STORAGE_ERROR");
    }

    public StorageException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.BAD_GATEWAY, "STORAGE_ERROR", properties);
    }
}
