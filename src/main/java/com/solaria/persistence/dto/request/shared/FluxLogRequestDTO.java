package com.solaria.persistence.dto.request.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FluxLogRequestDTO {

    @NotNull(message = "ID do usuário é obrigatório")
    private UUID userId;

    @NotBlank(message = "Ação é obrigatória")
    private String action;

}
