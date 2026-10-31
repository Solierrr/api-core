package com.solaria.persistence.dto.response.execution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solaria.persistence.domain.enums.execution.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code url} is only present on creation; the server keeps just the hash of the token. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectInviteLinkResponse(
        UUID id,
        ProjectRole role,
        String functionName,
        ProjectInviteLinkStatus status,
        OffsetDateTime expiresAt,
        Integer maxUses,
        int useCount,
        String url) {}
