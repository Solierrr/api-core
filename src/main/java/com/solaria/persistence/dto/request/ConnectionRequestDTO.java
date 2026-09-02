package com.solaria.persistence.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectionRequestDTO {

    @NotNull(message = "ID do usuário a ser conectado é obrigatório")
    private UUID connectedUserId;
}
