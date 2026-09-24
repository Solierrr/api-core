package com.solaria.persistence.dto.response.proposal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;
import com.solaria.persistence.dto.response.catalog.OfferResponseDTO;

@Getter
@Setter
public class ProposalItemResponseDTO {

    private UUID id;
    private UUID proposalId;
    private OfferResponseDTO offer;
    private Integer quantity;
    private BigDecimal negotiatedPrice;
    private BigDecimal discount;

}
