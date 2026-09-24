package com.solaria.persistence.dto.response.shared;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FluxLogResponseDTO {

    private UUID id;
    private UUID userId;
    private String action;
    private OffsetDateTime createdAt;

}
