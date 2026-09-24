package com.solaria.persistence.dto.patch.execution;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ServiceAcceptanceDTO {

    @NotNull(message = "Ator do aceite é obrigatório")
    private UUID acceptedBy;

}
