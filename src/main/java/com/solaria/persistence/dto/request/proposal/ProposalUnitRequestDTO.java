package com.solaria.persistence.dto.request.proposal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ProposalUnitRequestDTO {

    @NotNull(message = "ID do Item da Proposta é obrigatório")
    private UUID proposalItemId;

    @NotNull(message = "ID da Unidade Local é obrigatório")
    private UUID localUnitId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer quantity;

    private String note;

}
