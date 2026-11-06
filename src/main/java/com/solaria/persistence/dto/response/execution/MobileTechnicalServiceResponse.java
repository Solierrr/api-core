package com.solaria.persistence.dto.response.execution;

import com.solaria.persistence.domain.entity.execution.ProjectLocation;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MobileTechnicalServiceResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String purpose,
        ServiceStatus status,
        OffsetDateTime scheduledDate,
        OffsetDateTime acceptedAt,
        OffsetDateTime endDate,
        ProjectLocation location,
        boolean canManage,
        boolean hasExecutors,
        boolean managedByAgenda) {}
