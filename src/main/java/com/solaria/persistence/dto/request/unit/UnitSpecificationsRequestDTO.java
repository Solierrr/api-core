package com.solaria.persistence.dto.request.unit;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UnitSpecificationsRequestDTO {

    @NotNull(message = "ID da Unidade Local é obrigatório")
    private UUID localUnitId;

    private String specifications;

    private String locationPhotos;

}
