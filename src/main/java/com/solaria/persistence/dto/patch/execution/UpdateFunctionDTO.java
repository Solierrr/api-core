package com.solaria.persistence.dto.patch.execution;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateFunctionDTO {

    @NotBlank(message = "Função do colaborador é obrigatória")
    private String function;

}
