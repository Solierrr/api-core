package com.solaria.persistence.dto.request.identity;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record GrantPlatformAdminRequestDTO(@NotNull UUID userId) {
}
