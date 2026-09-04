package com.solaria.persistence.dto.response.professional;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import com.solaria.persistence.dto.response.identity.PersonResponseDTO;

@Getter
@Setter
public class TechnicianResponseDTO {

    private UUID id;
    private String crea;
    private String slug;
    private PersonResponseDTO person;

}
