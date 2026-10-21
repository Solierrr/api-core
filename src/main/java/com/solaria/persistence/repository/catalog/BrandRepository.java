package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.Brand;
import com.solaria.persistence.domain.enums.catalog.BrandVisibility;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Brand b where b.id = :id")
    Optional<Brand> lockById(UUID id);

    Optional<Brand> findByNormalizedNameAndVisibility(
            String normalizedName, BrandVisibility visibility);

    boolean existsByOwnerCompanyIdAndNormalizedNameAndArchivedFalseAndIdNot(
            UUID companyId, String name, UUID id);

    List<Brand> findByVisibilityAndArchivedFalseOrOwnerCompanyId(
            BrandVisibility visibility, UUID companyId);
}
