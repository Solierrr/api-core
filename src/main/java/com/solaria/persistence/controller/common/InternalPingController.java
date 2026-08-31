package com.solaria.persistence.controller.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint para testar se auth funcionou, tem apenas um return por ser apenas uma checagem rápida
 */
@RestController
public class InternalPingController {

    @GetMapping("/internal/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
}
