package com.solaria.persistence.dto.request.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PositionRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 12)
    private String name;

    @NotBlank(message = "Acessos são obrigatórios")
    private String accesses;

}
