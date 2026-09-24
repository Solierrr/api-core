package com.solaria.persistence.dto.response.identity;

import com.solaria.persistence.domain.enums.shared.PhotoType;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserPhotoResponseDTO {

    private UUID id;
    private UUID userId;
    private PhotoType type;
    private String url;
    private OffsetDateTime createdAt;

}
