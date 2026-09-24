package com.solaria.persistence.dto.request.proposal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProposalItemRequestDTO {

    @NotNull(message = "ID da Proposta é obrigatório")
    private UUID proposalId;

    @NotNull(message = "ID da Oferta é obrigatório")
    private UUID offerId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer quantity;

    @PositiveOrZero(message = "Preço negociado não pode ser negativo")
    private BigDecimal negotiatedPrice;

    @PositiveOrZero(message = "Desconto não pode ser negativo")
    @DecimalMax(value = "100", message = "Desconto percentual não pode exceder 100")
    private BigDecimal discount;

}
