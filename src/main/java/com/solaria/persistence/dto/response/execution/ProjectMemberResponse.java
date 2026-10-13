package com.solaria.persistence.dto.response.execution;

import com.solaria.persistence.domain.enums.execution.ProjectRole;

import java.util.UUID;

public record ProjectMemberResponse(
        UUID id, UUID userId, UUID authId, String name, ProjectRole role, String functionName) {}
