package com.solaria.persistence.dto.response.execution;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;

@Getter
@Setter
public class RequesterResponseDTO {

    private UUID id;
    private CompanyResponseDTO company;
    private String businessType;

}
