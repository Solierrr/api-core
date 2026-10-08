package com.solaria.persistence.dto.patch.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ServiceAcceptanceDTO {

    @Schema(description = "Campo legado opcional; o servidor ignora este valor e resolve o ator pelo JWT.",
            deprecated = true)
    private UUID acceptedBy;

}
