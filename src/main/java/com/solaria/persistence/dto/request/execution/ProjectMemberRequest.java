package com.solaria.persistence.dto.request.execution;

import com.solaria.persistence.domain.enums.execution.ProjectRole;

import jakarta.validation.constraints.*;

public record ProjectMemberRequest(
        @NotNull ProjectRole role, @Size(max = 120) String functionName) {}
