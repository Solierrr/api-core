package com.solaria.persistence.dto.request.execution;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceExecutorRequestDTO {

    @NotNull(message = "ID do Serviço é obrigatório")
    private UUID serviceId;

    @NotNull(message = "ID de Afiliação do Técnico é obrigatório")
    private UUID technicianAffiliationId;

    @NotBlank(message = "Função do colaborador é obrigatória")
    private String function;

}
