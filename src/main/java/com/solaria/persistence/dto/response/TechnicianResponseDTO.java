package com.solaria.persistence.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TechnicianResponseDTO {

    private UUID id;
    private String crea;
    private String slug;
    private PersonResponseDTO person;

}
