package com.solaria.persistence.exception;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message, List<Map<String, Object>> items) {
        super(message, HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", Map.of("items", items));
    }
}
