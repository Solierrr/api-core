package com.solaria.persistence.exception;

import org.springframework.http.HttpStatus;

public class UserNotProvisionedException extends BusinessException {

    public UserNotProvisionedException() {
        super("Usuário autenticado ainda não provisionado na api-core", HttpStatus.NOT_FOUND,
                "USER_NOT_PROVISIONED");
    }
}
