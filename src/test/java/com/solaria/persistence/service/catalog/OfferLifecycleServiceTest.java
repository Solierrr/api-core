package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.solaria.persistence.domain.entity.catalog.*;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.catalog.OfferStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.repository.catalog.*;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

class OfferLifecycleServiceTest {
    private final OfferRepository offers = mock(OfferRepository.class);
    private final OfferStatusHistoryRepository history = mock(OfferStatusHistoryRepository.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final RbacAuthorizationService rbac = mock(RbacAuthorizationService.class);
    private final OfferLifecycleService service = new OfferLifecycleService(offers,history,current,rbac);
    private Offer fixture() {
        var company = new Company(); company.setId(UUID.randomUUID());
        var supplier = new Supplier(); supplier.setCompany(company);
        var offer = new Offer(); offer.setId(UUID.randomUUID()); offer.setSupplier(supplier); offer.setAvailability(12);
        var user = new User(); user.setId(UUID.randomUUID()); when(current.getCurrentUser()).thenReturn(user);
        when(offers.lockById(offer.getId())).thenReturn(Optional.of(offer)); return offer;
    }
    @Test void pauseAndResumePreserveStockAndRecordActor() {
        var offer = fixture();
        service.change(offer.getId(),OfferStatus.PAUSED); service.change(offer.getId(),OfferStatus.ACTIVE);
        assertEquals(12,offer.getAvailability());
        var captured = org.mockito.ArgumentCaptor.forClass(OfferStatusHistory.class);
        verify(history,times(2)).save(captured.capture());
        assertEquals(OfferStatus.PAUSED,captured.getAllValues().get(0).getNewStatus());
        assertEquals(current.getCurrentUser().getId(),captured.getValue().getActorId());
        verify(rbac,times(2)).requireOwnCompany(offer.getSupplier().getCompany().getId());
    }
    @Test void closedCannotReopenAndRepeatedCloseDoesNotDuplicateHistory() {
        var offer = fixture(); service.change(offer.getId(),OfferStatus.CLOSED);
        service.change(offer.getId(),OfferStatus.CLOSED);
        assertThrows(BusinessRuleException.class,()->service.change(offer.getId(),OfferStatus.ACTIVE));
        verify(history).save(any());
    }
    @Test void foreignCompanyCannotMutateOrCreateHistory() {
        var offer = fixture(); doThrow(new BusinessRuleException("denied")).when(rbac).requireOwnCompany(any());
        assertThrows(BusinessRuleException.class,()->service.change(offer.getId(),OfferStatus.PAUSED));
        assertEquals(OfferStatus.ACTIVE,offer.getCommercialStatus()); verifyNoInteractions(history);
    }
}
