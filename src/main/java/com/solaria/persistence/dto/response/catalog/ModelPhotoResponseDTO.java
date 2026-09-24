package com.solaria.persistence.dto.response.catalog;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ModelPhotoResponseDTO {

    private UUID id;
    private UUID modelId;
    private String url;
    private OffsetDateTime createdAt;

}
