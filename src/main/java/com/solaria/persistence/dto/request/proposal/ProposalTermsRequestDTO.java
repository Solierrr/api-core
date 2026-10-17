package com.solaria.persistence.dto.request.proposal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProposalTermsRequestDTO {
    @NotEmpty(message = "Itens são obrigatórios")
    @Valid
    private List<@NotNull ProposalCommercialItemRequestDTO> items;

    @Size(max = 500, message = "Notas devem ter até 500 caracteres")
    private String notes;
}
