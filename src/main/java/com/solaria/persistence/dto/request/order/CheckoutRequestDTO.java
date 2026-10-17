package com.solaria.persistence.dto.request.order;

import com.solaria.persistence.domain.enums.company.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequestDTO(
        @NotNull(message = "Método de pagamento é obrigatório")
        PaymentMethod paymentMethod) {
}
