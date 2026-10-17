package com.solaria.persistence.dto.response.execution;

import com.solaria.persistence.domain.entity.execution.ProjectLocation;
import com.solaria.persistence.domain.enums.execution.*;

import java.time.OffsetDateTime;
import java.util.*;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UUID ownerCompanyId,
        UUID ownerUserId,
        String ownerName,
        ServiceStatus status,
        boolean archived,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID localUnitId,
        ProjectLocation location,
        ProjectRole role,
        boolean canManage,
        boolean canContribute,
        OffsetDateTime createdAt) {}
