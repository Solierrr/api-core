package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Recurso pedido não existe -> {@code 404 RESOURCE_NOT_FOUND}
 *
 * <p>Use as propriedades extras para devolver o identificador buscado</p>
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public ResourceNotFoundException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND", properties);
    }
}
