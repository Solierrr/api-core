package com.solaria.persistence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google-registry")
public class GoogleTranslateProperties {
    private String baseUrl = "http://localhost:8000";
    private String consumerToken = "";
    private String serviceKey = "";
    private int timeoutSeconds = 30;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getConsumerToken() { return consumerToken; }
    public void setConsumerToken(String consumerToken) { this.consumerToken = consumerToken; }
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
