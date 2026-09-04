package com.solaria.persistence.dto.response.unit;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class EnergyBillResponseDTO {

    private UUID id;
    private UUID localUnitId;
    private BigDecimal consumption;
    private BigDecimal price;
    private String photoUrl;

}
