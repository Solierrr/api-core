package com.solaria.persistence.dto.response.order;

import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.domain.enums.order.OrderPaymentStatus;
import com.solaria.persistence.domain.enums.order.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal feePercentage,
        BigDecimal feeAmount,
        BigDecimal total,
        OffsetDateTime createdAt,
        OffsetDateTime canceledAt,
        Payment payment,
        List<Line> items) {

    public record Payment(
            PaymentMethod method,
            BigDecimal amount,
            OrderPaymentStatus status,
            OffsetDateTime paidAt,
            OffsetDateTime refundedAt) {
    }

    public record Line(
            UUID offerId,
            String offerSlug,
            String companyName,
            String modelName,
            BigDecimal unitPrice,
            BigDecimal discountPercentage,
            BigDecimal netUnitPrice,
            Integer quantity,
            BigDecimal lineTotal) {
    }
}
