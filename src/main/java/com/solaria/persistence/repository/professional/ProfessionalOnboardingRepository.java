package com.solaria.persistence.repository.professional;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import com.solaria.persistence.domain.entity.professional.ProfessionalOnboarding;

public interface ProfessionalOnboardingRepository extends JpaRepository<ProfessionalOnboarding, UUID> {
    Optional<ProfessionalOnboarding> findByAuthId(UUID authId);
    Optional<ProfessionalOnboarding> findByTechnicianId(UUID technicianId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProfessionalOnboarding p where p.authId = :authId")
    Optional<ProfessionalOnboarding> lockByAuthId(UUID authId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProfessionalOnboarding p where p.id = :id")
    Optional<ProfessionalOnboarding> lockById(UUID id);
}
