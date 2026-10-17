package com.solaria.persistence.service.company;

import com.solaria.persistence.domain.entity.company.Charge;
import com.solaria.persistence.domain.entity.company.Subscription;
import com.solaria.persistence.dto.response.company.ChargeResponseDTO;
import com.solaria.persistence.dto.response.company.SubscriptionOverviewDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.ChargeRepository;
import com.solaria.persistence.repository.company.SubscriptionRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SubscriptionOverviewService {

    private final SubscriptionRepository subscriptionRepository;
    private final ChargeRepository chargeRepository;
    private final RbacAuthorizationService rbac;

    public SubscriptionOverviewService(SubscriptionRepository subscriptionRepository,
                                       ChargeRepository chargeRepository,
                                       RbacAuthorizationService rbac) {
        this.subscriptionRepository = subscriptionRepository;
        this.chargeRepository = chargeRepository;
        this.rbac = rbac;
    }

    @Transactional(readOnly = true)
    public SubscriptionOverviewDTO current() {
        List<Subscription> subscriptions = subscriptionRepository.findBySupplier_Company_Id(rbac.getCurrentCompanyId());
        Subscription subscription = subscriptions.stream()
                .max(Comparator.comparing(Subscription::getStartDate))
                .orElseThrow(() -> new ResourceNotFoundException("A empresa não possui assinatura"));
        List<ChargeResponseDTO> charges = chargeRepository.findBySubscriptionId(subscription.getId()).stream()
                .sorted(Comparator.comparing(Charge::getDueDate).reversed())
                .map(this::toCharge)
                .toList();
        return new SubscriptionOverviewDTO(subscription.getId(), subscription.getPlan().getName(),
                subscription.getPlan().getValue(), subscription.getPlan().getCycle(), subscription.getStatus(),
                subscription.getAutoRenewal(), subscription.getStartDate(), subscription.getEndDate(), charges);
    }

    private ChargeResponseDTO toCharge(Charge charge) {
        ChargeResponseDTO dto = new ChargeResponseDTO();
        dto.setId(charge.getId());
        dto.setSubscriptionId(charge.getSubscription().getId());
        dto.setAmount(charge.getAmount());
        dto.setPaymentMethod(charge.getPaymentMethod());
        dto.setStatus(charge.getStatus());
        dto.setDueDate(charge.getDueDate());
        dto.setPaymentDate(charge.getPaymentDate());
        return dto;
    }
}
