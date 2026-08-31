package com.solaria.persistence.dto.request.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequestDTO {

    @NotBlank(message = "Nome da permissão é obrigatório")
    @Size(max = 100)
    private String permissionName;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 300)
    private String description;

}
