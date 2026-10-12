package com.solaria.persistence.dto.request.unit;

import com.solaria.persistence.domain.enums.unit.LocationType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalUnitUpdateRequestDTO {
    private UUID addressId;
    private String complement;
    @NotNull(message = "Tipo de localização é obrigatório")
    private LocationType locationType;
}
