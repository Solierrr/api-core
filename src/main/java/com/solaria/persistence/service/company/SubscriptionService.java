package com.solaria.persistence.service.company;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.dto.response.company.CompanyPlansResponseDTO;
import com.solaria.persistence.dto.request.company.SubscriptionRequestDTO;
import com.solaria.persistence.dto.response.company.SubscriptionResponseDTO;
import com.solaria.persistence.domain.entity.company.CompanyPlans;
import com.solaria.persistence.domain.entity.company.Subscription;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.enums.company.SubscriptionStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyPlansRepository;
import com.solaria.persistence.repository.company.SubscriptionRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
public class SubscriptionService {

    private static final Map<SubscriptionStatus, Set<SubscriptionStatus>> ALLOWED_TRANSITIONS = Map.of(
            SubscriptionStatus.PAID, Set.of(SubscriptionStatus.IN_DEBT),
            SubscriptionStatus.IN_DEBT, Set.of(SubscriptionStatus.SUSPENDED, SubscriptionStatus.PAID),
            SubscriptionStatus.SUSPENDED, Set.of(SubscriptionStatus.PAID)
    );

    private final SubscriptionRepository subscriptionRepository;
    private final SupplierRepository supplierRepository;
    private final CompanyPlansRepository companyPlansRepository;
    private final RbacAuthorizationService rbac;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               SupplierRepository supplierRepository,
                               CompanyPlansRepository companyPlansRepository,
                               RbacAuthorizationService rbac) {
        this.subscriptionRepository = subscriptionRepository;
        this.supplierRepository = supplierRepository;
        this.companyPlansRepository = companyPlansRepository;
        this.rbac = rbac;
    }

    @Transactional
    public SubscriptionResponseDTO save(SubscriptionRequestDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fornecedor não encontrado com ID: " + dto.getSupplierId()));

        CompanyPlans plan = companyPlansRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plano não encontrado com ID: " + dto.getPlanId()));

        if (subscriptionRepository.hasActiveSubscription(dto.getSupplierId(), OffsetDateTime.now())) {
            throw new DuplicateResourceException(
                    "Fornecedor já possui assinatura ativa: " + dto.getSupplierId());
        }

        Subscription subscription = new Subscription();
        subscription.setSupplier(supplier);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.PAID);
        subscription.setAutoRenewal(dto.getAutoRenewal());
        subscription.setStartDate(OffsetDateTime.now());
        subscription.setEndDate(null);

        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponseDTO markInDebt(UUID id) {
        return applyTransition(id, SubscriptionStatus.IN_DEBT);
    }

    @Transactional
    public SubscriptionResponseDTO suspend(UUID id) {
        return applyTransition(id, SubscriptionStatus.SUSPENDED);
    }

    @Transactional
    public SubscriptionResponseDTO reactivate(UUID id) {
        return applyTransition(id, SubscriptionStatus.PAID);
    }

    @Transactional
    public SubscriptionResponseDTO end(UUID id) {
        Subscription subscription = findEntityById(id);
        rbac.requireOwnCompany(subscription.getSupplier().getCompany().getId());
        validateNotEnded(subscription);

        subscription.setEndDate(OffsetDateTime.now());
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDTO findById(UUID id, UUID companyId) {
        Subscription subscription = subscriptionRepository.findByIdAndSupplier_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assinatura não encontrada com ID: " + id));
        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> findBySupplier(UUID supplierId) {
        return subscriptionRepository.findBySupplierId(supplierId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> findAllByCompany(UUID companyId) {
        return subscriptionRepository.findBySupplier_Company_Id(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isSupplierSubscriptionActive(UUID supplierId) {
        return subscriptionRepository.hasActiveSubscription(supplierId, OffsetDateTime.now());
    }

    private Subscription findEntityById(UUID id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assinatura não encontrada com ID: " + id));
    }

    private void validateNotEnded(Subscription subscription) {
        if (subscription.getEndDate() != null) {
            throw new BusinessRuleException(
                    "Assinatura encerrada não pode sofrer novas transições: " + subscription.getId());
        }
    }

    private SubscriptionResponseDTO applyTransition(UUID id, SubscriptionStatus target) {
        Subscription subscription = findEntityById(id);
        rbac.requireOwnCompany(subscription.getSupplier().getCompany().getId());
        validateNotEnded(subscription);
        validateTransition(subscription.getStatus(), target);

        subscription.setStatus(target);
        return toResponse(subscriptionRepository.save(subscription));
    }

    private void validateTransition(SubscriptionStatus current, SubscriptionStatus target) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + current + " → " + target);
        }
    }

    private SubscriptionResponseDTO toResponse(Subscription subscription) {
        SubscriptionResponseDTO response = new SubscriptionResponseDTO();
        response.setId(subscription.getId());
        response.setSupplierId(subscription.getSupplier().getId());
        response.setPlan(toPlanResponse(subscription.getPlan()));
        response.setStatus(subscription.getStatus());
        response.setAutoRenewal(subscription.getAutoRenewal());
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        return response;
    }

    private CompanyPlansResponseDTO toPlanResponse(CompanyPlans plan) {
        if (plan == null) {
            return null;
        }
        CompanyPlansResponseDTO dto = new CompanyPlansResponseDTO();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setValue(plan.getValue());
        dto.setCycle(plan.getCycle());
        return dto;
    }
}
