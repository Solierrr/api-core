package com.solaria.persistence.dto.response.company;

import java.util.UUID;

public record CatalogCompanyResponseDTO(
        UUID id,
        String tradeName,
        String slug,
        String city,
        String state,
        String logoUrl,
        String bannerUrl,
        java.util.List<String> activities,
        boolean hasTechnicalTeam) {
}
