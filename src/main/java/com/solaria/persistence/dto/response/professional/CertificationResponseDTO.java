package com.solaria.persistence.dto.response.professional;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CertificationResponseDTO {

    private UUID id;
    private String name;
    private String issuer;
    private OffsetDateTime validity;
    private String description;

}
