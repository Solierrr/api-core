package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.solaria.persistence.domain.entity.catalog.*;
import com.solaria.persistence.domain.enums.catalog.*;
import com.solaria.persistence.dto.request.catalog.OfferRequestDTO;
import com.solaria.persistence.repository.catalog.*;
import com.solaria.persistence.repository.proposal.ProposalItemRepository;
import com.solaria.persistence.service.company.SubscriptionService;

class OfferServiceTranslationTest {
    private final OfferRepository offers = mock(OfferRepository.class);
    private final OfferTranslationRepository translations = mock(OfferTranslationRepository.class);
    private final OfferTranslationQueue translator = mock(OfferTranslationQueue.class);
    private final SubscriptionService subscriptions = mock(SubscriptionService.class);
    private final Offer offer = new Offer();
    private final OfferTranslation original = new OfferTranslation();
    private final OfferRequestDTO request = new OfferRequestDTO();
    private OfferService service;
    @BeforeEach void setup() {
        var supplier = new Supplier(); supplier.setId(UUID.randomUUID()); supplier.setStatus(SupplierStatus.ACTIVE);
        var model = new Model(); model.setId(UUID.randomUUID()); var brand = new com.solaria.persistence.domain.entity.catalog.Brand(); brand.setName("Brand"); model.setBrandEntry(brand); model.setModel("Code"); model.setType(PanelType.MONOCRYSTALLINE);
        offer.setId(UUID.randomUUID()); offer.setSupplier(supplier); offer.setModel(model);
        offer.setSourceLocale("pt-BR"); offer.setTranslationStatus(TranslationStatus.COMPLETED);
        original.setId(UUID.randomUUID()); original.setOffer(offer); original.setLocale("pt-BR");
        original.setTitle("Painel"); original.setDescription("Descricao");
        request.setSupplierId(supplier.getId()); request.setModelId(model.getId()); request.setTitle("Painel"); request.setDescription("Descricao");
        request.setUnitPrice(BigDecimal.TEN); request.setAvailability(2);
        when(offers.lockById(offer.getId())).thenReturn(Optional.of(offer));
        when(offers.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(translations.findByOfferId(offer.getId())).thenReturn(List.of(original));
        when(subscriptions.isSupplierSubscriptionActive(supplier.getId())).thenReturn(true);
        service = new OfferService(offers, mock(SupplierRepository.class), mock(ModelRepository.class), mock(BrandService.class),
                mock(ProposalItemRepository.class), subscriptions, translations, translator);
    }
    @Test void priceOnlyEditDoesNotSpendAnotherTranslation() {
        service.update(offer.getId(), request);
        verifyNoInteractions(translator); verify(translations, never()).deleteAll(any());
        assertEquals(TranslationStatus.COMPLETED, offer.getTranslationStatus());
    }
    @Test void textEditReplacesOldTranslationsAndStartsNewWork() {
        request.setTitle("Novo painel"); request.setDetails("Detalhes novos");
        service.update(offer.getId(), request);
        verify(translations).deleteAll(List.of(original)); verify(translations).flush();
        var capture = org.mockito.ArgumentCaptor.forClass(OfferTranslation.class); verify(translations).save(capture.capture());
        assertEquals("und", capture.getValue().getLocale()); assertEquals("Novo painel", capture.getValue().getTitle());
        assertEquals("Detalhes novos", capture.getValue().getDetails());
        assertEquals(TranslationStatus.PENDING, offer.getTranslationStatus()); assertNull(offer.getSourceLocale());
        verify(translator).enqueue(eq(offer.getId()), any());
    }
    @Test void deletionWithHistoryReturnsDomainConflictWithoutErasingAudit() {
        when(offers.existsStatusHistory(offer.getId())).thenReturn(true);
        assertThrows(com.solaria.persistence.exception.ResourceInUseException.class,()->service.deleteById(offer.getId()));
        verify(translations,never()).deleteAll(any()); verify(offers,never()).deleteById(any());
    }
    @Test void priceOnlyEditOfFailedTranslationDoesNotRestartPaidWork() {
        offer.setTranslationStatus(TranslationStatus.FAILED);
        service.update(offer.getId(),request); verifyNoInteractions(translator);
        assertEquals(TranslationStatus.FAILED,offer.getTranslationStatus());
    }
}

