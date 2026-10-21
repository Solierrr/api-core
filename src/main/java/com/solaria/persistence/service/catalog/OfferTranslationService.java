package com.solaria.persistence.service.catalog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.solaria.persistence.domain.entity.catalog.OfferTranslation;
import com.solaria.persistence.domain.enums.catalog.TranslationStatus;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.catalog.OfferTranslationRepository;
import com.solaria.persistence.util.SupportedLocales;

@Service
public class OfferTranslationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfferTranslationService.class);
    public static final String PENDING_LOCALE = "und";
    private record Source(UUID id, String title, String description, String details) {}
    private final OfferRepository offerRepository;
    private final OfferTranslationRepository offerTranslationRepository;
    private final GoogleTranslateService googleTranslateService;
    private final TransactionTemplate transaction;

    public OfferTranslationService(OfferRepository offerRepository,
            OfferTranslationRepository offerTranslationRepository,
            GoogleTranslateService googleTranslateService, PlatformTransactionManager manager) {
        this.offerRepository = offerRepository;
        this.offerTranslationRepository = offerTranslationRepository;
        this.googleTranslateService = googleTranslateService;
        this.transaction = new TransactionTemplate(manager);
    }

    public void translateOffer(UUID offerId) {
        translateVersion(offerId, null);
    }

    public String failVersion(UUID offerId, UUID sourceId) {
        return transaction.execute(status -> {
            var offer = offerRepository.lockById(offerId).orElse(null);
            if (offer == null) return "CANCELLED";
            if (offer.getTranslationStatus() == TranslationStatus.COMPLETED) return "COMPLETED";
            var pending = offerTranslationRepository.findByOfferIdAndLocale(offerId, PENDING_LOCALE).orElse(null);
            if (pending == null || !Objects.equals(pending.getId(), sourceId)) return "CANCELLED";
            offer.setTranslationStatus(TranslationStatus.FAILED);
            offerRepository.save(offer);
            return "FAILED";
        });
    }

    public String translateVersion(UUID offerId, UUID expectedSourceId) {
        var alreadyCompleted = transaction.execute(status -> offerRepository.findById(offerId)
                .map(offer -> offer.getTranslationStatus() == TranslationStatus.COMPLETED).orElse(false));
        if (Boolean.TRUE.equals(alreadyCompleted)) return "COMPLETED";
        Source source = transaction.execute(status -> offerTranslationRepository.findByOfferIdAndLocale(offerId, PENDING_LOCALE)
                .map(item -> new Source(item.getId(), item.getTitle(), item.getDescription(), item.getDetails())).orElse(null));
        if (source == null) {
            var completed = transaction.execute(status -> offerRepository.findById(offerId)
                    .map(offer -> offer.getTranslationStatus() == TranslationStatus.COMPLETED).orElse(false));
            return Boolean.TRUE.equals(completed) ? "COMPLETED" : "CANCELLED";
        }
        if (expectedSourceId != null && !Objects.equals(expectedSourceId, source.id())) return "CANCELLED";
        try {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("title", source.title());
            fields.put("description", source.description());
            if (source.details() != null && !source.details().isBlank()) fields.put("details", source.details());
            GoogleTranslateService.Result result = googleTranslateService.translateFields(fields);
            return transaction.execute(status -> {
                var offer = offerRepository.lockById(offerId).orElse(null);
                if (offer == null) return "CANCELLED";
                if (offer.getTranslationStatus() == TranslationStatus.COMPLETED) return "COMPLETED";
                var pending = offerTranslationRepository.findByOfferIdAndLocale(offerId, PENDING_LOCALE).orElse(null);
                if (pending == null || !Objects.equals(pending.getId(), source.id())) return "CANCELLED";
                boolean supportedSource = result.translations().containsKey(result.sourceLanguage());
                if (supportedSource) {
                    pending.setLocale(SupportedLocales.resolveFromShortCode(result.sourceLanguage()));
                    offerTranslationRepository.save(pending);
                    offer.setSourceLocale(pending.getLocale());
                } else {
                    offer.setSourceLocale(PENDING_LOCALE);
                }
                for (var entry : result.translations().entrySet()) {
                    if (supportedSource && entry.getKey().equals(result.sourceLanguage())) continue;
                    OfferTranslation translated = new OfferTranslation();
                    translated.setOffer(offer);
                    translated.setLocale(SupportedLocales.resolveFromShortCode(entry.getKey()));
                    translated.setTitle(entry.getValue().get("title"));
                    translated.setDescription(entry.getValue().get("description"));
                    translated.setDetails(fields.containsKey("details") ? entry.getValue().get("details") : source.details());
                    offerTranslationRepository.save(translated);
                }
                offer.setTranslationStatus(TranslationStatus.COMPLETED);
                offerRepository.save(offer);
                return "COMPLETED";
            });
        } catch (RuntimeException exception) {
            LOGGER.warn("Offer translation failed for {}", offerId);
            transaction.executeWithoutResult(status -> {
                var offer = offerRepository.lockById(offerId).orElse(null);
                var pending = offerTranslationRepository.findByOfferIdAndLocale(offerId, PENDING_LOCALE).orElse(null);
                if (offer != null && offer.getTranslationStatus() != TranslationStatus.COMPLETED && pending != null && Objects.equals(pending.getId(), source.id())) {
                    offer.setTranslationStatus(TranslationStatus.FAILED);
                    offerRepository.save(offer);
                }
            });
            return "FAILED";
        }
    }
}
