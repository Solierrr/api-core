package com.solaria.persistence.exception;

import org.springframework.http.HttpStatus;

public class AnalyticsUnavailableException extends BusinessException {

    public AnalyticsUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "ANALYTICS_UNAVAILABLE");
    }
}
