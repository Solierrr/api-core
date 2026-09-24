package com.solaria.persistence.dto.patch.identity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UpdatePositionDTO {

    @NotNull(message = "ID do Cargo é obrigatório")
    private UUID positionId;

}
