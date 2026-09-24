package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.Profession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ProfessionRepository extends JpaRepository<Profession, UUID> {
}
