package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface CertificationRepository extends JpaRepository<Certification, UUID> {
}
