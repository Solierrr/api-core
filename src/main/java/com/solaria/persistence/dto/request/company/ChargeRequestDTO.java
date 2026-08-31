package com.solaria.persistence.dto.request.company;

import com.solaria.persistence.domain.enums.company.PaymentMethod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ChargeRequestDTO {

    @NotNull(message = "ID da Assinatura é obrigatório")
    private UUID subscriptionId;

    @NotNull(message = "Valor do pagamento é obrigatório")
    @Positive(message = "Valor do pagamento deve ser maior que zero")
    private BigDecimal amount;

    @NotNull(message = "Método de pagamento é obrigatório")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Data de vencimento é obrigatória")
    @FutureOrPresent(message = "Data de vencimento não pode estar no passado")
    private LocalDate dueDate;

}
