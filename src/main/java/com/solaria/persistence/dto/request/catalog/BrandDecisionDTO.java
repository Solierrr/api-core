package com.solaria.persistence.dto.request.catalog;

import jakarta.validation.constraints.*;

public record BrandDecisionDTO(@NotNull Long revision, @Size(max = 2000) String reason) {}
