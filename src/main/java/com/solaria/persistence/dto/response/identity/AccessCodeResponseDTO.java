package com.solaria.persistence.dto.response.identity;

import java.time.Instant;
import java.util.UUID;

import com.solaria.persistence.domain.enums.identity.AccessCodeStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessCodeResponseDTO {

    private UUID id;
    private UUID companyId;
    private String code;
    private AccessCodeStatus status;
    private Instant expiresAt;
    private PositionResponseDTO position;

}
