package com.solaria.persistence.integration.auth;

// Lançada quando a chamada a POST /auth/login em api-auth falha
public class AuthIntegrationException extends RuntimeException {
    public AuthIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
