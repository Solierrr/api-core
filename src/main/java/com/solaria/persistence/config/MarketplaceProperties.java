package com.solaria.persistence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.marketplace")
public record MarketplaceProperties(BigDecimal feePercentage) {

    public MarketplaceProperties {
        if (feePercentage == null) {
            feePercentage = new BigDecimal("0.005");
        }
    }
}
