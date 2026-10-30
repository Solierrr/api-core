package com.solaria.persistence.dto.response.professional;

import java.time.OffsetDateTime;

import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;

public record AccreditedTechnicianResponseDTO(
        String technicianName,
        String affiliatedCompany,
        TechnicalAffiliationType affiliationType,
        String profession,
        String council,
        String registrationNumber,
        OffsetDateTime registrationExpirationDate,
        String city) {
}
