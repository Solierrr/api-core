package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TechnicianRepository extends JpaRepository<Technician, UUID> {

    boolean existsByPersonId(UUID personId);

    boolean existsBySlug(String slug);

    Optional<Technician> findBySlug(String slug);
}
