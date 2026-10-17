package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface OfferRepository extends JpaRepository<Offer, UUID> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT EXISTS(SELECT 1 FROM offer_status_history WHERE offer_id = :id)", nativeQuery = true)
    boolean existsStatusHistory(@org.springframework.data.repository.query.Param("id") UUID id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select o from Offer o where o.id = :id")
    Optional<Offer> lockById(@org.springframework.data.repository.query.Param("id") UUID id);

    List<Offer> findBySupplier_Company_Id(UUID companyId);

    Optional<Offer> findByIdAndSupplier_Company_Id(UUID id, UUID companyId);

    List<Offer> findBySupplierId(UUID supplierId);

    boolean existsByModelId(UUID modelId);

    boolean existsBySlug(String slug);

    Optional<Offer> findBySlugAndSupplier_Company_Slug(String slug, String companySlug);

    List<Offer> findByExpirationDateIsNullOrExpirationDateAfter(OffsetDateTime now);
}
