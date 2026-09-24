package com.solaria.persistence.dto.request.unit;

import com.solaria.persistence.domain.enums.unit.LocationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class LocalUnitRequestDTO {

    @NotNull(message = "ID do Solicitante é obrigatório")
    private UUID requesterId;

    private UUID addressId;

    private String complement;

    @NotNull(message = "Tipo de localização é obrigatório")
    private LocationType locationType;

}
