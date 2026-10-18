package com.solaria.persistence.service.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class OrderPricing {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private OrderPricing() {
    }

    static BigDecimal netUnitPrice(BigDecimal unitPrice, BigDecimal discountPercentage) {
        BigDecimal discount = discountPercentage == null ? BigDecimal.ZERO : discountPercentage;
        return unitPrice.multiply(HUNDRED.subtract(discount)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    static BigDecimal lineTotal(BigDecimal netUnitPrice, int quantity) {
        return netUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal fee(BigDecimal subtotal, BigDecimal feePercentage) {
        return subtotal.multiply(feePercentage).setScale(2, RoundingMode.HALF_UP);
    }
}
