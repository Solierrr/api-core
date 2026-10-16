package com.solaria.persistence.service.order;

import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.identity.PersonRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PurchaseEligibilityService {

    private final UserCompanyRepository userCompanyRepository;
    private final SupplierRepository supplierRepository;
    private final PersonRepository personRepository;
    private final TechnicianRepository technicianRepository;

    public PurchaseEligibilityService(UserCompanyRepository userCompanyRepository,
                                      SupplierRepository supplierRepository,
                                      PersonRepository personRepository,
                                      TechnicianRepository technicianRepository) {
        this.userCompanyRepository = userCompanyRepository;
        this.supplierRepository = supplierRepository;
        this.personRepository = personRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional(readOnly = true)
    public Set<UUID> requireBuyer(User user) {
        List<UserCompany> links = userCompanyRepository.findAllByUserId(user.getId());
        boolean belongsToSupplier = links.stream()
                .anyMatch(link -> supplierRepository.existsByCompanyId(link.getCompany().getId()));
        if (belongsToSupplier && !isApprovedProfessional(user)) {
            throw new UnauthorizedAccessException("Usuário de empresa fornecedora não pode comprar");
        }
        if (links.isEmpty() && !isApprovedProfessional(user)) {
            throw new UnauthorizedAccessException("Compras exigem uma empresa ou um registro profissional aprovado");
        }
        return links.stream().map(link -> link.getCompany().getId()).collect(Collectors.toSet());
    }

    public void requireNotOwnOffer(Set<UUID> buyerCompanyIds, Offer offer) {
        if (buyerCompanyIds.contains(offer.getSupplier().getCompany().getId())) {
            throw new UnauthorizedAccessException("Não é possível comprar uma oferta da própria empresa");
        }
    }

    private boolean isApprovedProfessional(User user) {
        return personRepository.findByUserId(user.getId())
                .flatMap(person -> technicianRepository.findByPersonId(person.getId()))
                .map(technician -> technician.getStatus() == TechnicianStatus.APPROVED)
                .orElse(false);
    }
}
