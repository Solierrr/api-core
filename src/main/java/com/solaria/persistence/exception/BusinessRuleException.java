package com.solaria.persistence.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Regra de negócio violada -> {@code 422 BUSINESS_RULE_VIOLATION}
 *
 * <p>Ex.: transição de status inválida, estoque insuficiente</p>
 */
public class BusinessRuleException extends BusinessException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleException(String message, Map<String, Object> properties) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT, "BUSINESS_RULE_VIOLATION", properties);
    }
}
