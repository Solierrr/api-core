package com.solaria.persistence.dto.request.identity;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PositionPermissionRequestDTO {

    @NotNull(message = "ID do Perfil é obrigatório")
    private UUID positionId;

    @NotNull(message = "ID da Permissão é obrigatório")
    private UUID permissionId;

}
