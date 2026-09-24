package com.solaria.persistence.dto.response.company;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import com.solaria.persistence.dto.response.identity.PositionResponseDTO;

@Getter
@Setter
public class CompanyPositionsResponseDTO {

    private UUID id;
    private UUID companyId;
    private PositionResponseDTO position;

}
