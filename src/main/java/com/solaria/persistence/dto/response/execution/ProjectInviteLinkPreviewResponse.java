package com.solaria.persistence.dto.response.execution;

import com.solaria.persistence.domain.enums.execution.ProjectRole;

import java.time.OffsetDateTime;

public record ProjectInviteLinkPreviewResponse(
        String projectName,
        ProjectRole role,
        String functionName,
        String invitedBy,
        OffsetDateTime expiresAt) {}
