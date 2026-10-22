package com.solaria.persistence.dto.response;

import java.util.List;

public record ConnectionPeopleResponse(List<ConnectionPersonResponse> items, boolean hasMore) {}
