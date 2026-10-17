package com.solaria.persistence.dto.response.catalog;

import com.solaria.persistence.domain.entity.catalog.Brand;
import com.solaria.persistence.domain.enums.catalog.BrandVisibility;

import java.util.UUID;

public record BrandResponseDTO(
        UUID id,
        UUID ownerCompanyId,
        String name,
        BrandVisibility visibility,
        boolean archived,
        boolean legacy,
        String logoUrl,
        String referenceUrl,
        long revision,
        boolean pending) {
    public static BrandResponseDTO from(Brand b, boolean pending) {
        return new BrandResponseDTO(
                b.getId(),
                b.getOwnerCompanyId(),
                b.getName(),
                b.getVisibility(),
                b.isArchived(),
                b.isLegacy(),
                b.getLogoUrl(),
                b.getReferenceUrl(),
                b.getRevision(),
                pending);
    }

    public static BrandResponseDTO from(Brand b) {
        return new BrandResponseDTO(
                b.getId(),
                b.getOwnerCompanyId(),
                b.getName(),
                b.getVisibility(),
                b.isArchived(),
                b.isLegacy(),
                b.getLogoUrl(),
                b.getReferenceUrl(),
                b.getRevision(),
                false);
    }
}
