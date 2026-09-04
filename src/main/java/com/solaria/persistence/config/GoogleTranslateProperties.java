package com.solaria.persistence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciais da Google Cloud Translation API (v2).
 */

// Habilita essa classe com as variaveis em application.properties com prefixo google-translate
@ConfigurationProperties(prefix = "google-translate")
public class GoogleTranslateProperties {

    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
