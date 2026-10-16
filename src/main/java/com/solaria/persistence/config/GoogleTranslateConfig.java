package com.solaria.persistence.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GoogleTranslateProperties.class)
public class GoogleTranslateConfig {
    @Bean
    public RestClient googleTranslateRestClient(GoogleTranslateProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds())));
        return RestClient.builder().baseUrl(properties.getBaseUrl()).requestFactory(factory).build();
    }
}
