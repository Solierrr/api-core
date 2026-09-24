
package com.solaria.persistence.dto.request.shared;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeolocalizationRequestDTO {

    private UUID addressId;

    @NotNull(message = "Latitude é obrigatória")
    @DecimalMin(value = "-90", message = "Latitude deve estar entre -90 e 90")
    @DecimalMax(value = "90", message = "Latitude deve estar entre -90 e 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitude é obrigatória")
    @DecimalMin(value = "-180", message = "Longitude deve estar entre -180 e 180")
    @DecimalMax(value = "180", message = "Longitude deve estar entre -180 e 180")
    private BigDecimal longitude;

}
