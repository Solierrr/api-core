package com.solaria.persistence.dto.response.company;

import java.util.UUID;

public record CatalogCompanyResponseDTO(
        UUID id,
        String tradeName,
        String slug,
        String city,
        String state) {
}
