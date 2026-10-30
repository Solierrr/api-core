package com.solaria.persistence.exception;

import org.springframework.http.HttpStatus;

public class RecommendationUnavailableException extends BusinessException {

    public RecommendationUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "RECOMMENDATION_UNAVAILABLE");
    }
}
