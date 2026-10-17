package com.solaria.persistence.service.catalog;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.OfferStatusHistory;
import com.solaria.persistence.domain.enums.catalog.OfferStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.*;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
@RequiredArgsConstructor
public class OfferLifecycleService {
    private final OfferRepository offers;
    private final OfferStatusHistoryRepository history;
    private final CurrentUserService current;
    private final RbacAuthorizationService rbac;

    @Transactional
    public OfferStatus change(UUID id, OfferStatus target) {
        Offer offer = offers.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Oferta não encontrada"));
        rbac.requireOwnCompany(offer.getSupplier().getCompany().getId());
        if (target == null) throw new BusinessRuleException("Situação obrigatória");
        var previous = offer.getCommercialStatus();
        if (previous == target) return target;
        if (previous == OfferStatus.CLOSED) throw new BusinessRuleException("Oferta encerrada não pode ser reaberta");
        var entry = new OfferStatusHistory();
        entry.setOfferId(id);
        entry.setPreviousStatus(previous);
        entry.setNewStatus(target);
        entry.setActorId(current.getCurrentUser().getId());
        offer.setCommercialStatus(target);
        offers.save(offer);
        history.save(entry);
        return target;
    }

    @Transactional(readOnly = true)
    public List<OfferStatusHistory> history(UUID id) {
        var offer = offers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Oferta não encontrada"));
        rbac.requireOwnCompany(offer.getSupplier().getCompany().getId());
        return history.findByOfferIdOrderByCreatedAtDesc(id);
    }
}
