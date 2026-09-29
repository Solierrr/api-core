package com.solaria.persistence.dto.response.company;

import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;

@Getter
@Setter
public class CompanyResponseDTO {

    private UUID id;
    private CompanyStatus status;
    private CompanyType type;
    private String cnpj;
    private String tradeName;
    private String corporateName;
    private String slug;
    private AddressResponseDTO address;
    private BusinessContactResponseDTO businessContact;

}
