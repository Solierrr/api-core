package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Usuário autenticado sem permissão sobre o recurso -> {@code 403 UNAUTHORIZED_ACCESS}
 *
 * <p>Regra de posse checada no serviço; o 403 de RBAC sai pelo {@code ProblemDetailAccessDeniedHandler}</p>
 */
public class UnauthorizedAccessException extends BusinessException {

    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public UnauthorizedAccessException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN", properties);
    }
}
