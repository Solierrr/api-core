package com.solaria.persistence.dto.request.professional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TechnicianRequestDTO {

    @NotNull(message = "ID da Pessoa é obrigatório")
    private UUID personId;

    @NotBlank(message = "CREA é obrigatório")
    private String crea;

}
