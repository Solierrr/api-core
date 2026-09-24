package com.solaria.persistence.dto.response.professional;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProfessionalReviewResponseDTO {

    private UUID id;
    private UUID professionalId;
    private UUID reviewerId;
    private UUID serviceId;
    private BigDecimal rating;
    private String comment;
    private boolean active;
    private OffsetDateTime createdAt;

}
