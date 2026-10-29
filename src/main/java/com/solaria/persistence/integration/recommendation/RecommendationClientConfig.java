package com.solaria.persistence.integration.recommendation;

import java.net.http.HttpClient;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

// factory responsável por criar as chamadas http ao api-recommendation
@Configuration
@EnableConfigurationProperties(RecommendationClientProperties.class)
public class RecommendationClientConfig {

    @Bean
    public RestClient recommendationRestClient(RecommendationClientProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);
        if (properties.enabled()) {
            builder.baseUrl(properties.baseUrl());
        }
        return builder.build();
    }
}
