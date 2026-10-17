package com.solaria.persistence.repository.professional;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.solaria.persistence.domain.entity.professional.ProfessionalDocument;

public interface ProfessionalDocumentRepository extends JpaRepository<ProfessionalDocument, UUID> {
    long countByUserIdAndCreatedAtAfter(UUID userId, java.time.Instant createdAt);
}
