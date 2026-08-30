package com.solaria.persistence.config;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private String namespace = "core";

    private SupplierSearch supplierSearch = new SupplierSearch();

    @Getter
    @Setter
    public static class SupplierSearch {

        private Duration ttlMin = Duration.ofMinutes(12);

        private Duration ttlMax = Duration.ofMinutes(18);

        private int popularityThreshold = 3;

        private int maxCacheablePage = 2;

        private int maxCacheableSize = 50;

        private Duration counterTtl = Duration.ofHours(1);
        
    }
}