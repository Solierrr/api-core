package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

public interface TechnicianRepository extends JpaRepository<Technician, UUID> {

    @Override
    @EntityGraph(attributePaths = {"person", "person.user", "person.contact"})
    Optional<Technician> findById(UUID id);

    @EntityGraph(attributePaths = {"person", "person.user", "person.contact"})
    List<Technician> findByPersonUserId(UUID userId);

    boolean existsByPersonId(UUID personId);

    boolean existsBySlug(String slug);

    Optional<Technician> findBySlug(String slug);
}
