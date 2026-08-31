package com.solaria.persistence.dto.request.unit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class EnergyBillRequestDTO {

    @NotNull(message = "ID da Unidade Local é obrigatório")
    private UUID localUnitId;

    @NotNull(message = "Consumo é obrigatório")
    @Positive(message = "Consumo deve ser maior que zero")
    private BigDecimal consumption;

    @NotNull(message = "Preço é obrigatório")
    @PositiveOrZero(message = "Preço não pode ser negativo")
    private BigDecimal price;

}
