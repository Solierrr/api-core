package com.solaria.persistence.dto.request.execution;

import com.solaria.persistence.domain.enums.execution.ProjectRole;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record ProjectInvitationRequest(
        @Email @Size(max = 254) String email,
        UUID teamId,
        @NotNull ProjectRole role,
        @Size(max = 120) String functionName,
        @Pattern(regexp = "pt-BR|en-US|es-ES") String locale) {}
