package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.Technician;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TechnicianRepository extends JpaRepository<Technician, UUID> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from Technician t where t.id=:id")
    java.util.Optional<Technician> findByIdForUpdate(java.util.UUID id);

    Optional<Technician> findByPersonId(UUID personId);

    boolean existsByPersonId(UUID personId);

    boolean existsBySlug(String slug);

    Optional<Technician> findBySlug(String slug);
}
