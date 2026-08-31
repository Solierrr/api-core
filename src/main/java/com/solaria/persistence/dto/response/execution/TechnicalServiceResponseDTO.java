package com.solaria.persistence.dto.response.execution;

import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TechnicalServiceResponseDTO {

    private UUID id;
    private UUID technicalProjectId;
    private String purpose;
    private ServiceStatus status;
    private OffsetDateTime scheduledDate;
    private OffsetDateTime createdAt;
    private UUID acceptedBy;
    private OffsetDateTime acceptedAt;
    private OffsetDateTime endDate;

}
