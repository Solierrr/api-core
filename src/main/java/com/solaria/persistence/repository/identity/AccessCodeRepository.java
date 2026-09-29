package com.solaria.persistence.repository.identity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solaria.persistence.domain.entity.identity.AccessCode;

public interface AccessCodeRepository extends JpaRepository<AccessCode, UUID> {

    Optional<AccessCode> findByCode(String code);

    boolean existsByCode(String code);

    List<AccessCode> findByCompanyId(UUID companyId);

    Optional<AccessCode> findByIdAndCompanyId(UUID id, UUID companyId);

}
