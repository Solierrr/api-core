package com.solaria.persistence.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.solaria.persistence.config.RedisProperties;
import com.solaria.persistence.dto.request.SupplierSearchFilterDTO;
import com.solaria.persistence.dto.response.SupplierSearchResponseDTO;
import com.solaria.persistence.util.CacheKeyGenerator;

@Service
public class SupplierCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupplierCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheKeyGenerator cacheKeyGenerator;
    private final RedisProperties redisProperties;

    public SupplierCacheService(
            RedisTemplate<String, Object> redisTemplate,
            StringRedisTemplate stringRedisTemplate,
            CacheKeyGenerator cacheKeyGenerator,
            RedisProperties redisProperties) {

        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.redisProperties = redisProperties;

    }

    public Optional<SupplierSearchResponseDTO> find(SupplierSearchFilterDTO filters) {

        if (!isCacheEligible(filters)) {
            return Optional.empty();
        }

        String key = cacheKeyGenerator.supplierSearch(filters);

        try {
            Object cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                return Optional.empty();
            }

            if (cachedValue instanceof SupplierSearchResponseDTO response) {
                return Optional.of(response);
            }

            LOGGER.warn(
                    "Tipo inesperado no cache de fornecedores para a chave {}",
                    key);

            return Optional.empty();

        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Falha ao consultar o cache de fornecedores",
                    exception);

            return Optional.empty();
        }
    }

    public void handleCacheMiss(SupplierSearchFilterDTO filters, SupplierSearchResponseDTO response) {
        
        if (!isCacheEligible(filters)) {
            return;
        }

        String counterKey = cacheKeyGenerator.searchCounter(filters);

        try {
            Long count = stringRedisTemplate
            .opsForValue()
            .increment(counterKey);

            if (count == null) {
                return;
            }

            RedisProperties.SupplierSearch properties = redisProperties.getSupplierSearch();

            if (count == 1L) {
                stringRedisTemplate.expire(counterKey, properties.getCounterTtl());
            }

            if (count >= properties.getPopularityThreshold()) {
                save(filters, response);
            }
        } catch (RuntimeException exception) {
            LOGGER.warn(
                "Falha ao atualizar a popularidade da pesquisa",
                exception
            );
        }
    }

    private void save(
            SupplierSearchFilterDTO filters,
            SupplierSearchResponseDTO response) {

        String key = cacheKeyGenerator.supplierSearch(filters);

        try {
            redisTemplate.opsForValue().set(
                    key,
                    response,
                    randomSearchTtl());
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Falha ao armazenar a pesquisa no cache",
                    exception);
        }
    }

    private boolean isCacheEligible(
            SupplierSearchFilterDTO filters) {

        RedisProperties.SupplierSearch properties = redisProperties.getSupplierSearch();

        return filters.getPage() >= 0
                && filters.getPage() <= properties.getMaxCacheablePage()
                && filters.getSize() > 0
                && filters.getSize() <= properties.getMaxCacheableSize();
    }

    private Duration randomSearchTtl() {
        RedisProperties.SupplierSearch properties = redisProperties.getSupplierSearch();

        long minimum = properties.getTtlMin().toMillis();
        long maximum = properties.getTtlMax().toMillis();

        if (maximum <= minimum) {
            return Duration.ofMillis(Math.max(1, minimum));
        }

        long ttl = ThreadLocalRandom.current()
                .nextLong(minimum, maximum + 1);

        return Duration.ofMillis(ttl);
    }
}
