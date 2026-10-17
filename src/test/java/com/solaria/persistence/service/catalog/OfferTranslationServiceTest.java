package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.OfferTranslation;
import com.solaria.persistence.domain.enums.catalog.TranslationStatus;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.catalog.OfferTranslationRepository;

class OfferTranslationServiceTest {
    private final OfferRepository offers = mock(OfferRepository.class);
    private final OfferTranslationRepository translations = mock(OfferTranslationRepository.class);
    private final GoogleTranslateService registry = mock(GoogleTranslateService.class);
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final UUID id = UUID.randomUUID();
    private final Offer offer = new Offer();
    private final OfferTranslation pending = new OfferTranslation();
    private OfferTranslationService service;

    @BeforeEach
    void setup() {
        when(manager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        offer.setId(id); pending.setId(UUID.randomUUID()); pending.setOffer(offer);
        pending.setLocale("und"); pending.setTitle("Painel"); pending.setDescription("Descricao");
        when(translations.findByOfferIdAndLocale(id, "und")).thenReturn(Optional.of(pending));
        service = new OfferTranslationService(offers, translations, registry, manager);
    }
    private GoogleTranslateService.Result result() {
        return new GoogleTranslateService.Result("pt", Map.of(
                "pt", Map.of("title", "Painel", "description", "Descricao"),
                "en", Map.of("title", "Panel", "description", "Description"),
                "es", Map.of("title", "Panel solar", "description", "Descripcion")));
    }
    @Test
    void translatesNamedTextFieldsAndWritesSupportedLocales() {
        when(offers.lockById(id)).thenReturn(Optional.of(offer));
        when(registry.translateFields(any())).thenReturn(result());
        service.translateOffer(id);
        assertEquals(TranslationStatus.COMPLETED, offer.getTranslationStatus());
        assertEquals("pt-BR", offer.getSourceLocale());
        verify(registry).translateFields(Map.of("title", "Painel", "description", "Descricao"));
        var saved = org.mockito.ArgumentCaptor.forClass(OfferTranslation.class);
        verify(translations, times(3)).save(saved.capture());
        assertEquals(java.util.Set.of("pt-BR", "en-US", "es-ES"), saved.getAllValues().stream().map(OfferTranslation::getLocale).collect(java.util.stream.Collectors.toSet()));
        assertTrue(saved.getAllValues().stream().allMatch(item -> item.getDetails() == null));
    }
    @Test
    void recordsFailureAndPreservesOriginalText() {
        when(offers.lockById(id)).thenReturn(Optional.of(offer));
        when(registry.translateFields(any())).thenThrow(new IllegalStateException("unavailable"));
        service.translateOffer(id);
        assertEquals(TranslationStatus.FAILED, offer.getTranslationStatus());
        assertEquals("und", pending.getLocale()); assertEquals("Painel", pending.getTitle());
        verify(translations, never()).save(any());
    }
    @Test
    void oldResultCannotOverwriteARecentlyEditedOffer() {
        var newer = new OfferTranslation(); newer.setId(UUID.randomUUID());
        when(offers.lockById(id)).thenReturn(Optional.of(offer));
        when(translations.findByOfferIdAndLocale(id, "und")).thenReturn(Optional.of(pending), Optional.of(newer));
        when(registry.translateFields(any())).thenReturn(result());
        service.translateOffer(id);
        verify(translations, never()).save(any()); verify(offers, never()).save(any());
        assertEquals(TranslationStatus.PENDING, offer.getTranslationStatus());
    }
    @Test void completedUnsupportedOriginDoesNotSpendAnotherProviderCallOnRecovery() {
        offer.setTranslationStatus(TranslationStatus.COMPLETED); offer.setSourceLocale("und");
        when(offers.findById(id)).thenReturn(Optional.of(offer));
        assertEquals("COMPLETED",service.translateVersion(id,pending.getId()));
        verifyNoInteractions(registry);
    }
}
