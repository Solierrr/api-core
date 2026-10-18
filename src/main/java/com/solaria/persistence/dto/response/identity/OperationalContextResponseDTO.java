package com.solaria.persistence.dto.response.identity;

import java.util.List;
import java.util.UUID;

import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;

/** Contexto operacional do usuário autenticado: empresa, situação dela, cargo e permissões. */
public record OperationalContextResponseDTO(
        UUID companyId,
        CompanyType companyType,
        CompanyStatus companyStatus,
        String positionName,
        boolean admin,
        List<String> permissions) {
}
