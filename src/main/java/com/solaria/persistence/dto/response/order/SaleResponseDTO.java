package com.solaria.persistence.dto.response.order;

import com.solaria.persistence.domain.enums.order.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SaleResponseDTO(
        UUID orderId,
        UUID itemId,
        OrderStatus status,
        OffsetDateTime createdAt,
        String buyerUsername,
        UUID offerId,
        String offerSlug,
        String modelName,
        BigDecimal netUnitPrice,
        Integer quantity,
        BigDecimal lineTotal) {
}
