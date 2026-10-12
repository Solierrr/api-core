package com.solaria.persistence.dto.response.execution;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonalTechnicalServiceResponseDTO extends TechnicalServiceResponseDTO {

    private String requesterName;
    private PersonalTechnicalServiceAddressResponseDTO address;
    private PersonalTechnicalServiceGeoResponseDTO geo;
    private List<PersonalTechnicalServiceExecutorResponseDTO> executors = List.of();
    private String myRole;
    private List<String> allowedActions = List.of();
}
