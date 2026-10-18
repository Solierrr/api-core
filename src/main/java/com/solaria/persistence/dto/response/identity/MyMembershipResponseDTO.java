package com.solaria.persistence.dto.response.identity;

import java.util.List;
import java.util.UUID;

/** Vínculo do usuário autenticado com uma empresa, com o cargo e as permissões desse cargo. */
public record MyMembershipResponseDTO(UUID companyId, PositionSummary position, List<String> permissions) {

    public record PositionSummary(UUID id, String name) {
    }
}
