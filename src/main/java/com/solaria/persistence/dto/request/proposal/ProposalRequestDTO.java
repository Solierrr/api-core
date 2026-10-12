package com.solaria.persistence.dto.request.proposal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProposalRequestDTO {

    @NotNull(message = "ID do ofertante é obrigatório")
    private UUID supplierId;

    @NotEmpty(message = "Itens são obrigatórios")
    @Valid
    @jakarta.validation.groups.ConvertGroup(to = ProposalCommercialItemRequestDTO.Creation.class)
    private List<@NotNull ProposalCommercialItemRequestDTO> items;

    @Size(max = 500, message = "Notas devem ter até 500 caracteres")
    private String notes;
}
