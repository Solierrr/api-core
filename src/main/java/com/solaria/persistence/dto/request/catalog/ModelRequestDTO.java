package com.solaria.persistence.dto.request.catalog;

import com.solaria.persistence.domain.enums.catalog.InfrastructureType;
import com.solaria.persistence.domain.enums.catalog.PanelType;
import com.solaria.persistence.domain.enums.catalog.ProductType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ModelRequestDTO {

    @NotNull(message = "Marca é obrigatória")
    private java.util.UUID brandId;

    @NotBlank(message = "Modelo é obrigatório")
    @jakarta.validation.constraints.Size(max=255)
    private String model;

    // PANEL (padrao) exige os campos da placa; INFRASTRUCTURE exige infrastructureType e nao aceita campos de placa
    private ProductType productType = ProductType.PANEL;

    private InfrastructureType infrastructureType;

    @jakarta.validation.constraints.Size(max=64)
    private String material;

    private PanelType type;

    private BigDecimal powerWp;

    @jakarta.validation.constraints.DecimalMax("100")
    private BigDecimal efficiency;

    private BigDecimal width;

    private BigDecimal length;

    private BigDecimal weight;

}
