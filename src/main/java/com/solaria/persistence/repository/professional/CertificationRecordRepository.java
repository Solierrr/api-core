package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.CertificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface CertificationRecordRepository extends JpaRepository<CertificationRecord, UUID> {

    List<CertificationRecord> findByProfessionalRegistrationId(UUID professionalRegistrationId);

    boolean existsByProfessionalRegistrationId(UUID professionalRegistrationId);

    boolean existsByCertificationId(UUID certificationId);
}
