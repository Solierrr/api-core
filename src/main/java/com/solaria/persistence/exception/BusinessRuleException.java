package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends BusinessException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", properties);
    }
}
