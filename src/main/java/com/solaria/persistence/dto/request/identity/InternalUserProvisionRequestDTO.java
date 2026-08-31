package com.solaria.persistence.dto.request.identity;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalUserProvisionRequestDTO {

    // authId é o sub do JWT emitido pelo api-auth; obrigatório para localizar/criar o User
    @NotNull(message = "authId é obrigatório")
    private UUID authId;
}
