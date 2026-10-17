package com.solaria.persistence.dto.response.professional;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import com.solaria.persistence.dto.response.identity.PersonResponseDTO;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;

@Getter
@Setter
public class TechnicianResponseDTO {

    private UUID id;
    private TechnicianStatus status;
    private String crea;
    private String slug;
    private PersonResponseDTO person;

}
