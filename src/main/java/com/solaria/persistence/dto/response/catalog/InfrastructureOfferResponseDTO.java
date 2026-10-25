package com.solaria.persistence.dto.response.catalog;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InfrastructureOfferResponseDTO(
        UUID offerId,
        String supplierTradeName,
        String brand,
        String model,
        String infrastructureType,
        String unitPrice,
        Integer availability,
        OffsetDateTime expirationDate) {
}
