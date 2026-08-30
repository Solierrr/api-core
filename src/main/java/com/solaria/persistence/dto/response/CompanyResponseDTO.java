package com.solaria.persistence.dto.response;

import com.solaria.persistence.domain.enums.CompanyStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CompanyResponseDTO {

    private UUID id;
    private CompanyStatus status;
    private String cnpj;
    private String tradeName;
    private String corporateName;
    private String slug;
    private AddressResponseDTO address;
    private BusinessContactResponseDTO businessContact;

}
