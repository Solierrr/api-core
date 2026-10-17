package com.solaria.persistence.dto.response.execution;

import com.solaria.persistence.domain.enums.execution.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectInvitationResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String email,
        ProjectRole role,
        String functionName,
        ProjectInvitationStatus status,
        OffsetDateTime expiresAt,
        String emailState) {}
