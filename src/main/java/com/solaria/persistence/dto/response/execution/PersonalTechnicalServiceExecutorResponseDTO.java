package com.solaria.persistence.dto.response.execution;

import java.util.UUID;

public record PersonalTechnicalServiceExecutorResponseDTO(
        UUID authId,
        String displayName,
        String function,
        String avatarUrl) {
}
