package com.solaria.persistence.dto.response;

import com.solaria.persistence.domain.enums.ModelStatus;
import com.solaria.persistence.domain.enums.PanelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelResponseDTO {

    private UUID id;
    private String brand;
    private String model;
    private PanelType type;
    private BigDecimal powerWp;
    private BigDecimal efficiency;
    private BigDecimal width;
    private BigDecimal length;
    private BigDecimal weight;
    private ModelStatus status;

}
