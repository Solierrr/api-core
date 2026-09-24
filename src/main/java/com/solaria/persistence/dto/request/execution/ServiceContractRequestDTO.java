package com.solaria.persistence.dto.request.execution;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class ServiceContractRequestDTO {

    @NotNull(message = "ID do Serviço é obrigatório")
    private UUID serviceId;

    private String warranty;

    @FutureOrPresent(message = "Prazo de entrega não pode estar no passado")
    private LocalDate deliveryDeadline;

    @NotNull(message = "Opção de seguro é obrigatório")
    private Boolean insurance;

}
