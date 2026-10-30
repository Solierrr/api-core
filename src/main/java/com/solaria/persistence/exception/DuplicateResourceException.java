package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Recurso já existe -> {@code 409 DUPLICATE_RESOURCE}
 *
 * <p>Ex.: e-mail ou documento já cadastrado</p>
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", properties);
    }
}
