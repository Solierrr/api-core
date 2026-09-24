package com.solaria.persistence.dto.response.catalog;

import com.solaria.persistence.domain.enums.catalog.SupplierStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;

@Getter
@Setter
public class SupplierResponseDTO {

    private UUID id;
    private CompanyResponseDTO company;
    private SupplierStatus status;
    private String businessType;

}
