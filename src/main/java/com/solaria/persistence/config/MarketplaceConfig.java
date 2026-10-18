package com.solaria.persistence.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MarketplaceProperties.class)
public class MarketplaceConfig {
}
