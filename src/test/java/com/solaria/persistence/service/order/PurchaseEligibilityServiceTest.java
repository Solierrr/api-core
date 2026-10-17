package com.solaria.persistence.service.order;

import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseEligibilityServiceTest {

    @Mock UserCompanyRepository userCompanyRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock TechnicianRepository technicianRepository;

    PurchaseEligibilityService service;
    User user;
    Company company;

    @BeforeEach
    void setUp() {
        service = new PurchaseEligibilityService(userCompanyRepository, supplierRepository, technicianRepository);
        user = new User();
        user.setId(UUID.randomUUID());
        company = new Company();
        company.setId(UUID.randomUUID());
        lenient().when(technicianRepository.findByPersonUserId(user.getId())).thenReturn(List.of());
    }

    private void linkToCompany() {
        UserCompany link = new UserCompany();
        link.setCompany(company);
        when(userCompanyRepository.findByUserId(user.getId())).thenReturn(Optional.of(link));
    }

    private void technician(TechnicianStatus status) {
        Technician technician = new Technician();
        technician.setStatus(status);
        when(technicianRepository.findByPersonUserId(user.getId())).thenReturn(List.of(technician));
    }

    @Test
    void demandantCompanyUserCanBuy() {
        linkToCompany();
        when(supplierRepository.existsByCompanyId(company.getId())).thenReturn(false);

        assertEquals(Set.of(company.getId()), service.requireBuyer(user));
    }

    @Test
    void supplierCompanyUserCannotBuy() {
        linkToCompany();
        when(supplierRepository.existsByCompanyId(company.getId())).thenReturn(true);

        assertThrows(UnauthorizedAccessException.class, () -> service.requireBuyer(user));
    }

    @Test
    void approvedProfessionalOfSupplierCompanyCanBuy() {
        linkToCompany();
        when(supplierRepository.existsByCompanyId(company.getId())).thenReturn(true);
        technician(TechnicianStatus.APPROVED);

        assertEquals(Set.of(company.getId()), service.requireBuyer(user));
    }

    @Test
    void professionalWithoutCompanyBuysOnlyWhenApproved() {
        when(userCompanyRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        assertThrows(UnauthorizedAccessException.class, () -> service.requireBuyer(user));

        technician(TechnicianStatus.UNDER_ANALYSIS);
        assertThrows(UnauthorizedAccessException.class, () -> service.requireBuyer(user));

        technician(TechnicianStatus.APPROVED);
        assertEquals(Set.of(), service.requireBuyer(user));
    }

    @Test
    void ownCompanyOfferIsRejected() {
        Supplier supplier = new Supplier();
        supplier.setCompany(company);
        Offer offer = new Offer();
        offer.setSupplier(supplier);

        assertThrows(UnauthorizedAccessException.class,
                () -> service.requireNotOwnOffer(Set.of(company.getId()), offer));
        service.requireNotOwnOffer(Set.of(UUID.randomUUID()), offer);
    }
}
