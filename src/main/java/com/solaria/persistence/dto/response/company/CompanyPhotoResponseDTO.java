package com.solaria.persistence.dto.response.company;

import com.solaria.persistence.domain.enums.shared.PhotoType;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class CompanyPhotoResponseDTO {

    private UUID id;
    private UUID companyId;
    private PhotoType type;
    private String url;
    private OffsetDateTime createdAt;

}
