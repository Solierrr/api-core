package com.solaria.persistence.dto.patch.execution;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdatePurposeDTO {

    @NotBlank(message = "Finalidade do serviço é obrigatória")
    private String purpose;

}
