package com.solaria.persistence.dto.request.execution;

import com.solaria.persistence.domain.entity.execution.ProjectLocation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 4000) String description,
        UUID ownerCompanyId,
        UUID localUnitId,
        @Valid ProjectLocation location,
        OffsetDateTime startDate,
        OffsetDateTime endDate) {}
