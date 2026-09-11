package com.solaria.persistence.repository.company;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.solaria.persistence.domain.entity.company.CompanyPositions;


public interface CompanyPositionsRepository extends JpaRepository<CompanyPositions, UUID> {

    @EntityGraph(attributePaths = "position")
    List<CompanyPositions> findByCompanyId(UUID companyId);

    @EntityGraph(attributePaths = "position")
    Optional<CompanyPositions> findByPositionId(UUID positionId);

    boolean existsByCompanyIdAndPositionId(UUID companyId, UUID positionId);

    boolean existsByCompanyId(UUID companyId);
}
