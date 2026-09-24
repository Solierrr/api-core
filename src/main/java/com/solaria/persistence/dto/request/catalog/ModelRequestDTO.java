package com.solaria.persistence.dto.request.catalog;

import com.solaria.persistence.domain.enums.catalog.PanelType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ModelRequestDTO {

    @NotBlank(message = "Marca é obrigatória")
    private String brand;

    @NotBlank(message = "Modelo é obrigatório")
    private String model;

    @NotNull(message = "Tipo da placa é obrigatório")
    private PanelType type;

    @NotNull(message = "Potência (Wp) é obrigatória")
    private BigDecimal powerWp;

    @NotNull(message = "Eficiência é obrigatória")
    private BigDecimal efficiency;

    @NotNull(message = "Largura é obrigatória")
    private BigDecimal width;

    @NotNull(message = "Comprimento é obrigatório")
    private BigDecimal length;

    @NotNull(message = "Peso é obrigatório")
    private BigDecimal weight;

}
