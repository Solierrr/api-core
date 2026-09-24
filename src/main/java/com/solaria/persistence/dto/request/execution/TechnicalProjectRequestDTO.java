package com.solaria.persistence.dto.request.execution;

import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
public class TechnicalProjectRequestDTO {

    private UUID requesterId;

    private UUID localUnitId;

    private ServiceStatus status;

    private OffsetDateTime startDate;

    private LocalDateTime endDate;

}
