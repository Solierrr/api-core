package com.solaria.persistence.security;

import java.util.LinkedHashMap;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.service-keys")
public class ServiceKeyProperties extends LinkedHashMap<String, String> {

    static final int MIN_KEY_LENGTH = 32;

    @PostConstruct
    public void validate() {
        forEach((service, key) -> {
            if (key != null && !key.isBlank() && key.length() < MIN_KEY_LENGTH) {
                throw new IllegalStateException("A chave de serviço de '" + service + "' (app.service-keys."
                        + service + ") deve ter pelo menos " + MIN_KEY_LENGTH + " caracteres");
            }
        });
    }
}
