package com.solaria.persistence.repository.professional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import com.solaria.persistence.domain.entity.professional.ProfessionalSubmission;

public interface ProfessionalSubmissionRepository extends JpaRepository<ProfessionalSubmission, UUID> {
    List<ProfessionalSubmission> findByOnboardingIdOrderByCreatedAtDesc(UUID onboardingId);
    List<ProfessionalSubmission> findByStatusOrderByCreatedAtAsc(String status, org.springframework.data.domain.Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProfessionalSubmission s where s.id = :id")
    Optional<ProfessionalSubmission> lockById(UUID id);
    @Query("select s.id from ProfessionalSubmission s where s.status in ('QUEUED', 'RETRY_PENDING', 'VALIDATING') and s.availableAt <= :now order by s.createdAt")
    List<UUID> findAvailable(Instant now, org.springframework.data.domain.Pageable pageable);
}
