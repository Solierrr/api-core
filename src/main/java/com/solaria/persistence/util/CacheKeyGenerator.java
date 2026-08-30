package com.solaria.persistence.util;

import org.springframework.stereotype.Component;
import com.solaria.persistence.config.RedisProperties;
import com.solaria.persistence.dto.request.SupplierSearchFilterDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;

@Component
public final class CacheKeyGenerator {

    private static final String CACHE_VERSION = "v1";

    private final RedisProperties redisProperties;

    public CacheKeyGenerator(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    public String supplierSearch(SupplierSearchFilterDTO filters) {
        return createKey(
                "search:supplier",
                canonicalize(filters, true));
    }

    public String searchCounter(SupplierSearchFilterDTO filters) {
        return createKey(
                "counter:search:supplier",
                canonicalize(filters, false));
    }

    private String createKey(String category, String canonicalValue) {
        return redisProperties.getNamespace()
                + ":" + CACHE_VERSION
                + ":" + category
                + ":" + sha256(canonicalValue);
    }

    private String canonicalize(SupplierSearchFilterDTO filters, boolean includePagination) {
        Objects.requireNonNull(filters, "Filtros não podem ser nulos");

        StringBuilder value = new StringBuilder();

        append(value, "query", normalize(filters.getQuery()));
        append(value, "state", normalize(filters.getState()));
        append(value, "city", normalize(filters.getCity()));
        append(value, "neighborhood", normalize(filters.getNeighborhood()));
        append(value, "businessType", normalize(filters.getBusinessType()));

        if (includePagination) {
            append(value, "page", Integer.toString(filters.getPage()));
            append(value, "size", Integer.toString(filters.getSize()));
        }

        return value.toString();

    }

    private void append(
            StringBuilder builder,
            String field,
            String value) {

        builder.append(field)
                .append(':')
                .append(value.length())
                .append(':')
                .append(value)
                .append('|');
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 não está disponível",
                    exception);
        }
    }
}
