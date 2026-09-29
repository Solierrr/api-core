package com.solaria.persistence.dto.request.identity;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessCodeRequestDTO {

    @NotNull(message = "ID da Empresa é obrigatório")
    private UUID companyId;

    @NotNull(message = "ID do Cargo é obrigatório")
    private UUID positionId;

}
