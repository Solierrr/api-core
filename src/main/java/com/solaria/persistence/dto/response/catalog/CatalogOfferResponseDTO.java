package com.solaria.persistence.dto.response.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CatalogOfferResponseDTO(
        UUID id,
        String slug,
        UUID companyId,
        String companySlug,
        String companyTradeName,
        String title,
        String description,
        String details,
        UUID modelId,
        String brand,
        String model,
        String productType,
        String infrastructureType,
        String material,
        String type,
        BigDecimal powerWp,
        BigDecimal efficiency,
        BigDecimal width,
        BigDecimal length,
        BigDecimal weight,
        BigDecimal unitPrice,
        BigDecimal discountPercentage,
        Integer availability,
        List<String> serviceRegions,
        List<String> photoUrls) {
}
