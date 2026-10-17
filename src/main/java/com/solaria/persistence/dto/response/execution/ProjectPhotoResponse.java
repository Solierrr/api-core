package com.solaria.persistence.dto.response.execution;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectPhotoResponse(
        UUID id, String name, String mime, boolean cover, long size, OffsetDateTime createdAt) {}
