package com.solaria.persistence.repository.catalog;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.solaria.persistence.domain.entity.catalog.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface OfferRepository extends JpaRepository<Offer, UUID> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select o from Offer o where o.id = :id")
    Optional<Offer> lockById(@org.springframework.data.repository.query.Param("id") UUID id);


    @Override
    @EntityGraph(attributePaths = {"supplier.company", "model"})
    Optional<Offer> findById(UUID id);

    @EntityGraph(attributePaths = {"supplier.company", "model"})
    List<Offer> findBySupplier_Company_Id(UUID companyId);

    @EntityGraph(attributePaths = {"supplier.company", "model"})
    Optional<Offer> findByIdAndSupplier_Company_Id(UUID id, UUID companyId);

    @EntityGraph(attributePaths = {"supplier.company", "model"})
    List<Offer> findBySupplierId(UUID supplierId);

    @EntityGraph(attributePaths = {"supplier", "model"})
    List<Offer> findByIdIn(List<UUID> ids);

    @Modifying(flushAutomatically = true)
    @Query("update Offer o set o.availability = "
            + "case when o.availability - :quantity < :remaining then o.availability - :quantity "
            + "else :remaining end where o.id = :id and o.availability >= :quantity "
            + "and (o.expirationDate is null or o.expirationDate >= :now)")
    int reserveAvailability(@Param("id") UUID id,
                            @Param("quantity") int quantity,
                            @Param("remaining") int remaining,
                            @Param("now") OffsetDateTime now);

    boolean existsByModelId(UUID modelId);

    boolean existsBySlug(String slug);

    List<Offer> findByExpirationDateIsNullOrExpirationDateAfter(OffsetDateTime now);
}
