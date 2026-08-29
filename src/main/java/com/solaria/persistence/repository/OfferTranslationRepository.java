package com.solaria.persistence.repository;

import com.solaria.persistence.domain.entity.OfferTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferTranslationRepository extends JpaRepository<OfferTranslation, UUID> {

    List<OfferTranslation> findByOfferId(UUID offerId);

    Optional<OfferTranslation> findByOfferIdAndLocale(UUID offerId, String locale);

    boolean existsByOfferIdAndLocale(UUID offerId, String locale);
}
