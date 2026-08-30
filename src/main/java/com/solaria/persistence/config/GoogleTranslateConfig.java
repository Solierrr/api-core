package com.solaria.persistence.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GoogleTranslateProperties.class)
public class GoogleTranslateConfig {

    @Bean
    public RestClient googleTranslateRestClient() {
        return RestClient.builder()
                .baseUrl("https://translation.googleapis.com/language/translate/v2")
                .build();
    }
}
