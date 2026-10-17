package com.solaria.persistence.dto.response.catalog;

import com.solaria.persistence.domain.entity.catalog.BrandRequest;
import com.solaria.persistence.domain.enums.catalog.*;

import java.time.Instant;
import java.util.UUID;

public record BrandRequestResponseDTO(
        UUID id,
        UUID brandId,
        UUID requesterCompanyId,
        BrandRequestKind kind,
        BrandRequestStatus status,
        String proposedName,
        String proposedLogoUrl,
        String proposedReferenceUrl,
        UUID resultBrandId,
        String decisionReason,
        Instant createdAt,
        Instant decidedAt,
        long revision) {
    public static BrandRequestResponseDTO from(BrandRequest r) {
        return new BrandRequestResponseDTO(
                r.getId(),
                r.getBrand().getId(),
                r.getRequesterCompanyId(),
                r.getKind(),
                r.getStatus(),
                r.getProposedName(),
                r.getProposedLogoUrl(),
                r.getProposedReferenceUrl(),
                r.getResultBrandId(),
                r.getDecisionReason(),
                r.getCreatedAt(),
                r.getDecidedAt(),
                r.getRevision());
    }
}
