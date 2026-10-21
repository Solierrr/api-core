package com.solaria.persistence.dto.response.execution;

import com.solaria.persistence.domain.enums.execution.*;

import java.time.OffsetDateTime;
import java.util.*;

public record ProjectActivityResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        ProjectActivityType type,
        ServiceStatus status,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        boolean onsite,
        List<UUID> assigneeIds,
        UUID technicalServiceId) {}
