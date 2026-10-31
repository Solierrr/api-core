package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.TechnicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, UUID> {
}
