package com.solaria.persistence.dto.request.execution;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequesterRequestDTO {

    @NotNull(message = "ID da Empresa é obrigatório")
    private UUID companyId;

    private String businessType;

}
