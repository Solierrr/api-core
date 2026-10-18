package com.solaria.persistence.repository.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.solaria.persistence.domain.entity.catalog.OfferStatusHistory;

public interface OfferStatusHistoryRepository extends JpaRepository<OfferStatusHistory,UUID> {
    List<OfferStatusHistory> findByOfferIdOrderByCreatedAtDesc(UUID offerId);
}
