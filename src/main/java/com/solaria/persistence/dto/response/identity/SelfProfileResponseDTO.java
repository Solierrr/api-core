package com.solaria.persistence.dto.response.identity;

import java.util.List;
import java.util.UUID;

import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;

/** Perfil pessoal sem username, dados civis ou permissões empresariais. */
public record SelfProfileResponseDTO(
        UUID id,
        UUID authId,
        String avatar,
        String banner,
        PersonContext person,
        TechnicianContext technician,
        List<ActiveAffiliation> activeAffiliations,
        List<CompanyMembership> companyMemberships) {

    public record PersonContext(String name, String phone) {
    }

    public record TechnicianContext(UUID id, List<ProfessionSummary> professions) {
    }

    public record ProfessionSummary(UUID id, String name) {
    }

    public record ActiveAffiliation(TechnicalAffiliationType type, UUID companyId, String companyName) {
    }

    public record CompanyMembership(UUID companyId, String companyName, String position) {
    }
}
