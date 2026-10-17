package com.solaria.persistence.dto.response.unit;

import com.solaria.persistence.domain.enums.unit.LocalUnitPhotoType;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class LocalUnitPhotoResponseDTO {

    private UUID id;
    private UUID localUnitId;
    private LocalUnitPhotoType type;
    private String url;
    private OffsetDateTime createdAt;

}
