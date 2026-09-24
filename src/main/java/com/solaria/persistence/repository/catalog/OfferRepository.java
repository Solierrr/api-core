package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface OfferRepository extends JpaRepository<Offer, UUID> {

    List<Offer> findBySupplier_Company_Id(UUID companyId);

    Optional<Offer> findByIdAndSupplier_Company_Id(UUID id, UUID companyId);

    List<Offer> findBySupplierId(UUID supplierId);

    boolean existsByModelId(UUID modelId);

    boolean existsBySlug(String slug);

    List<Offer> findByExpirationDateIsNullOrExpirationDateAfter(OffsetDateTime now);
}
