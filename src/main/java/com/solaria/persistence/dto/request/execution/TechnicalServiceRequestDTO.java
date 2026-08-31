package com.solaria.persistence.dto.request.execution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TechnicalServiceRequestDTO {

    @NotNull(message = "ID do Projeto Técnico é obrigatório")
    private UUID technicalProjectId;

    @NotBlank(message = "Finalidade do serviço é obrigatória")
    private String purpose;

    private OffsetDateTime scheduledDate;

}
