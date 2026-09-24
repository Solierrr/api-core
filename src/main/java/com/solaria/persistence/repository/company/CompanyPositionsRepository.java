package com.solaria.persistence.repository.company;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solaria.persistence.domain.entity.company.CompanyPositions;


public interface CompanyPositionsRepository extends JpaRepository<CompanyPositions, UUID> {

    List<CompanyPositions> findByCompanyId(UUID companyId);

    Optional<CompanyPositions> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndPositionId(UUID companyId, UUID positionId);

    boolean existsByPositionId(UUID positionId);

    boolean existsByCompanyId(UUID companyId);
}
