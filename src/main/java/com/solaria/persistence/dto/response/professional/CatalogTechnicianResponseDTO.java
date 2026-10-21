package com.solaria.persistence.dto.response.professional;

import java.util.List;
import java.util.UUID;

public record CatalogTechnicianResponseDTO(
        UUID id,
        String slug,
        String name,
        String crea,
        List<String> professions) {
}
