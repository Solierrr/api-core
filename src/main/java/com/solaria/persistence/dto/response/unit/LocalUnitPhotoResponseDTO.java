package com.solaria.persistence.dto.response.unit;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class LocalUnitPhotoResponseDTO {

    private UUID id;
    private UUID localUnitId;
    private String url;
    private OffsetDateTime createdAt;

}
