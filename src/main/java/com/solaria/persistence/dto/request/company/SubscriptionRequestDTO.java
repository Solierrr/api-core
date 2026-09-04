package com.solaria.persistence.dto.request.company;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class SubscriptionRequestDTO {

    @NotNull(message = "ID do Fornecedor é obrigatório")
    private UUID supplierId;

    @NotNull(message = "ID do Plano é obrigatório")
    private UUID planId;

    @NotNull(message = "Renovação automática do plano precisa ser preenchida")
    private Boolean autoRenewal;

}
