package com.solaria.persistence.dto.request.execution;

import com.solaria.persistence.domain.enums.execution.*;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.*;

public record ProjectActivityRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 4000) String description,
        @NotNull ProjectActivityType type,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt,
        boolean onsite,
        @NotNull @Size(max = 50) List<UUID> assigneeIds) {}
