package com.solaria.persistence.dto.response;

import java.util.UUID;

public record ConnectionPersonResponse(UUID id, String name, String username,
        String avatar, String banner, boolean connected) {}
