package com.solaria.persistence.dto.request.execution;

import jakarta.validation.constraints.*;

/** The token travels in the body so it never lands in access logs as part of a path. */
public record ProjectInviteLinkTokenRequest(@NotBlank @Size(max = 128) String token) {}
