package com.solaria.persistence.dto.request.identity;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCompanyRequestDTO {

    @NotNull(message = "ID da Empresa é obrigatório")
    private UUID companyId;

    @NotNull(message = "ID do Usuário é obrigatório")
    private UUID userId;

    @NotNull(message = "ID do Cargo é obrigatório")
    private UUID positionId;

}
