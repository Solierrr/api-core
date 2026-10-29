package com.solaria.persistence.integration.recommendation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

// Liga esta classe ao prefixo "app.integration.recommendation" do application.properties; sem baseUrl os feeds ficam desligados
@ConfigurationProperties(prefix = "app.integration.recommendation")
public record RecommendationClientProperties(
        String baseUrl,
        String apiKey,
        @DefaultValue("PT5S") Duration connectTimeout,
        @DefaultValue("PT25S") Duration readTimeout,
        @DefaultValue("PT60S") Duration cacheTtl) {

    public boolean enabled() {
        return baseUrl != null && !baseUrl.isBlank();
    }
}
