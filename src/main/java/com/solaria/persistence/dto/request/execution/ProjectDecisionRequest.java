package com.solaria.persistence.dto.request.execution;

import com.solaria.persistence.domain.enums.execution.ServiceStatus;

import jakarta.validation.constraints.NotNull;

public record ProjectDecisionRequest(@NotNull ServiceStatus status) {}
