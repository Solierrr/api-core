package com.solaria.persistence.dto.request.proposal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProposalCommercialItemRequestDTO {

    public interface Creation extends jakarta.validation.groups.Default {}

    @Null(groups = Creation.class, message = "ID não pode ser informado na criação")
    private UUID id;

    @NotNull(message = "Oferta é obrigatória")
    private UUID offerId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser positiva")
    private Integer quantity;

    @Positive(message = "Preço deve ser positivo")
    private BigDecimal negotiatedPrice;

    @DecimalMin(value = "0", message = "Desconto deve ser de 0 a 100")
    @DecimalMax(value = "100", message = "Desconto deve ser de 0 a 100")
    private BigDecimal discount;
}
