package com.solaria.persistence.integration.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// Liga esta classe ao prefixo "app.integration.auth" do application.properties
@ConfigurationProperties(prefix = "app.integration.auth")
public class AuthClientProperties {

    // URL de api-auth
    private String baseUrl;

    // timeout de conexão da chamada HTTP a api-auth
    private Duration connectTimeout = Duration.ofSeconds(5);

    // timeout de leitura da chamada HTTP a api-auth
    private Duration readTimeout = Duration.ofSeconds(5);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
