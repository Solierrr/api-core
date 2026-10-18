package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.BrandRequest;
import com.solaria.persistence.domain.enums.catalog.BrandRequestStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface BrandRequestRepository extends JpaRepository<BrandRequest, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BrandRequest r where r.id = :id")
    Optional<BrandRequest> lockById(UUID id);

    @Query(
            "select r.brand.id from BrandRequest r where r.status ="
                + " com.solaria.persistence.domain.enums.catalog.BrandRequestStatus.PENDING")
    List<UUID> findPendingBrandIds();

    boolean existsByBrandIdAndStatus(UUID brandId, BrandRequestStatus status);

    List<BrandRequest> findByRequesterCompanyIdOrderByCreatedAtDesc(UUID companyId);

    List<BrandRequest> findAllByOrderByCreatedAtDesc();
}
