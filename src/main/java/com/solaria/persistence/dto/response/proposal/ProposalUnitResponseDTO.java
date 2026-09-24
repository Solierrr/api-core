package com.solaria.persistence.dto.response.proposal;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import com.solaria.persistence.dto.response.unit.LocalUnitResponseDTO;

@Setter
@Getter
public class ProposalUnitResponseDTO {

    private UUID id;
    private UUID proposalItemId;
    private LocalUnitResponseDTO localUnit;
    private Integer quantity;
    private String note;

}
