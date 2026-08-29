package com.solaria.persistence.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class OfferRequestDTO {

    @NotNull(message = "ID do Fornecedor é obrigatório")
    private UUID supplierId;

    @NotNull(message = "ID do Modelo é obrigatório")
    private UUID modelId;

    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    private String details;

    @NotNull(message = "Preço unitário é obrigatório")
    @Positive(message = "Preço unitário deve ser maior que zero")
    private BigDecimal unitPrice;

    @NotNull(message = "Disponibilidade é obrigatória")
    @PositiveOrZero(message = "Disponibilidade não pode ser negativa")
    private Integer availability;

    private OffsetDateTime expirationDate;

    @DecimalMin(value = "0", message = "Percentual de desconto não pode ser negativo")
    @DecimalMax(value = "100", message = "Percentual de desconto não pode ser maior que 100")
    private BigDecimal discountPercentage;

    private List<String> serviceRegions;

}
