package com.solaria.persistence.dto.response.company;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BusinessContactResponseDTO {

    private UUID id;
    private String companyEmail;
    private String phone;
    private String website;

}
