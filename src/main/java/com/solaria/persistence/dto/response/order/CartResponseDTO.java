package com.solaria.persistence.dto.response.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponseDTO(
        List<Line> items,
        BigDecimal subtotal,
        BigDecimal feePercentage,
        BigDecimal feeAmount,
        BigDecimal total) {

    public record Line(
            UUID offerId,
            String offerSlug,
            String companySlug,
            String companyName,
            String modelName,
            BigDecimal unitPrice,
            BigDecimal discountPercentage,
            BigDecimal netUnitPrice,
            Integer quantity,
            BigDecimal lineTotal,
            Integer availability,
            boolean purchasable) {
    }
}
