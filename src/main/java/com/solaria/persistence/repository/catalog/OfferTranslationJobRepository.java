package com.solaria.persistence.repository.catalog;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.solaria.persistence.domain.entity.catalog.OfferTranslationJob;

public interface OfferTranslationJobRepository extends JpaRepository<OfferTranslationJob, UUID> {
    @Query(value = "SELECT * FROM offer_translation_job WHERE status IN ('QUEUED','RETRY_PENDING','PROCESSING') AND available_at <= CURRENT_TIMESTAMP ORDER BY available_at,id LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<OfferTranslationJob> claimAvailable();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from OfferTranslationJob j where j.id = :id")
    Optional<OfferTranslationJob> lockById(@Param("id") UUID id);

    @Modifying
    @Query("update OfferTranslationJob j set j.status = 'CANCELLED' where j.offerId = :offerId and j.status in ('QUEUED','RETRY_PENDING','PROCESSING')")
    void cancelOutstanding(@Param("offerId") UUID offerId);
}
