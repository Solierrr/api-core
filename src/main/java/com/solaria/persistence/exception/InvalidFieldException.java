package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Campo com valor inválido que a Bean Validation não cobre -> {@code 400 INVALID_FIELD}
 *
 * <p>Validação de formato/obrigatoriedade fica nas anotações do DTO ({@code VALIDATION_ERROR})</p>
 */
public class InvalidFieldException extends BusinessException {

    public InvalidFieldException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_FIELD");
    }

    public InvalidFieldException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_FIELD", properties);
    }
}
