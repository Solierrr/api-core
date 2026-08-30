package com.solaria.persistence.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.Offer;
import com.solaria.persistence.domain.entity.OfferTranslation;
import com.solaria.persistence.domain.enums.TranslationStatus;
import com.solaria.persistence.repository.OfferRepository;
import com.solaria.persistence.repository.OfferTranslationRepository;
import com.solaria.persistence.util.SupportedLocales;

/**
 * Preenche as traduções de uma oferta (placa solar) em background: detecta o idioma de origem
 * via Google Translate e traduz o conteúdo para os demais idiomas suportados.
 *
 * Disparado pelo OfferService após o commit da transação que cria a oferta (via
 * TransactionSynchronizationManager#registerSynchronization), para não bloquear a resposta
 * de criação nem competir com a transação de escrita original.
 */
@Service
public class OfferTranslationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfferTranslationService.class);

    /** Locale temporário gravado na criação da oferta, até a detecção do idioma real concluir. */
    public static final String PENDING_LOCALE = "und";

    private final OfferRepository offerRepository;
    private final OfferTranslationRepository offerTranslationRepository;
    private final GoogleTranslateService googleTranslateService;

    public OfferTranslationService(OfferRepository offerRepository,
                                   OfferTranslationRepository offerTranslationRepository,
                                   GoogleTranslateService googleTranslateService) {
        this.offerRepository = offerRepository;
        this.offerTranslationRepository = offerTranslationRepository;
        this.googleTranslateService = googleTranslateService;
    }

    @Async
    @Transactional
    public void translateOffer(UUID offerId) {
        try {
            Offer offer = offerRepository.findById(offerId).orElse(null);
            if (offer == null) {
                return;
            }

            OfferTranslation sourceTranslation = offerTranslationRepository
                    .findByOfferIdAndLocale(offerId, PENDING_LOCALE)
                    .orElse(null);
            if (sourceTranslation == null) {
                return;
            }

            String detectedShortCode = googleTranslateService.detectLanguage(
                    sourceTranslation.getTitle() + ". " + sourceTranslation.getDescription());
            String sourceLocale = SupportedLocales.resolveFromShortCode(detectedShortCode);

            sourceTranslation.setLocale(sourceLocale);
            offerTranslationRepository.save(sourceTranslation);
            offer.setSourceLocale(sourceLocale);

            List<String> sourceTexts = new ArrayList<>();
            sourceTexts.add(sourceTranslation.getTitle());
            sourceTexts.add(sourceTranslation.getDescription());
            sourceTexts.add(sourceTranslation.getDetails() == null ? "" : sourceTranslation.getDetails());

            String sourceShortCode = shortCode(sourceLocale);

            for (String targetLocale : SupportedLocales.targetsExcluding(sourceLocale)) {
                List<String> translated = googleTranslateService.translate(
                        sourceTexts, sourceShortCode, shortCode(targetLocale));

                OfferTranslation translation = new OfferTranslation();
                translation.setOffer(offer);
                translation.setLocale(targetLocale);
                translation.setTitle(translated.get(0));
                translation.setDescription(translated.get(1));
                translation.setDetails(sourceTranslation.getDetails() == null ? null : translated.get(2));
                offerTranslationRepository.save(translation);
            }

            offer.setTranslationStatus(TranslationStatus.COMPLETED);
            offerRepository.save(offer);
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao traduzir oferta {}", offerId, ex);
            offerRepository.findById(offerId).ifPresent(offer -> {
                offer.setTranslationStatus(TranslationStatus.FAILED);
                offerRepository.save(offer);
            });
        }
    }

    private String shortCode(String locale) {
        return locale.split("-")[0].toLowerCase();
    }
}
