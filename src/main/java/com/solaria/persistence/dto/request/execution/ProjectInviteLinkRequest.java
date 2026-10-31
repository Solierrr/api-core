package com.solaria.persistence.dto.request.execution;

import com.solaria.persistence.domain.enums.execution.ProjectRole;

import jakarta.validation.constraints.*;

public record ProjectInviteLinkRequest(
        @NotNull ProjectRole role,
        @Size(max = 120) String functionName,
        @Min(1) @Max(30) Integer expiresInDays,
        @Min(1) @Max(1000) Integer maxUses) {}
