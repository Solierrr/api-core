package com.solaria.persistence.service.order;

import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PurchaseEligibilityService {

    private final UserCompanyRepository userCompanyRepository;
    private final SupplierRepository supplierRepository;
    private final TechnicianRepository technicianRepository;

    public PurchaseEligibilityService(UserCompanyRepository userCompanyRepository,
                                      SupplierRepository supplierRepository,
                                      TechnicianRepository technicianRepository) {
        this.userCompanyRepository = userCompanyRepository;
        this.supplierRepository = supplierRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional(readOnly = true)
    public Set<UUID> requireBuyer(User user) {
        Optional<UserCompany> link = userCompanyRepository.findByUserId(user.getId());
        boolean belongsToSupplier = link
                .map(item -> supplierRepository.existsByCompanyId(item.getCompany().getId()))
                .orElse(false);
        if (belongsToSupplier && !isApprovedProfessional(user)) {
            throw new UnauthorizedAccessException("Usuário de empresa fornecedora não pode comprar");
        }
        if (link.isEmpty() && !isApprovedProfessional(user)) {
            throw new UnauthorizedAccessException("Compras exigem uma empresa ou um registro profissional aprovado");
        }
        return link.map(item -> Set.of(item.getCompany().getId())).orElse(Set.of());
    }

    public void requireNotOwnOffer(Set<UUID> buyerCompanyIds, Offer offer) {
        if (buyerCompanyIds.contains(offer.getSupplier().getCompany().getId())) {
            throw new UnauthorizedAccessException("Não é possível comprar uma oferta da própria empresa");
        }
    }

    private boolean isApprovedProfessional(User user) {
        return technicianRepository.findByPersonUserId(user.getId()).stream()
                .anyMatch(technician -> technician.getStatus() == TechnicianStatus.APPROVED);
    }
}
