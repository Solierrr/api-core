package com.solaria.persistence.service.company;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.company.CompanyPlans;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.request.company.CompanyRequestDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.BusinessContactRepository;
import com.solaria.persistence.repository.company.CompanyPhotoRepository;
import com.solaria.persistence.repository.company.CompanyPlansRepository;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.company.SubscriptionRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.professional.TechnicianAffiliationRepository;
import com.solaria.persistence.repository.shared.AddressRepository;
import com.solaria.persistence.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private BusinessContactRepository businessContactRepository;
    @Mock private UserCompanyRepository userCompanyRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private RequesterRepository requesterRepository;
    @Mock private TechnicianAffiliationRepository technicianAffiliationRepository;
    @Mock private CompanyPositionsRepository companyPositionsRepository;
    @Mock private CompanyPhotoRepository companyPhotoRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private CompanyPlansRepository companyPlansRepository;
    @Mock private SubscriptionRepository subscriptionRepository;

    private CompanyService service;

    @BeforeEach
    void setUp() {
        service = new CompanyService(companyRepository, addressRepository, businessContactRepository,
                userCompanyRepository, supplierRepository, requesterRepository, technicianAffiliationRepository,
                companyPositionsRepository, companyPhotoRepository, positionRepository, currentUserService,
                companyPlansRepository, subscriptionRepository);
    }

    private CompanyRequestDTO supplierDto() {
        CompanyRequestDTO dto = new CompanyRequestDTO();
        dto.setType(CompanyType.SUPPLIER);
        dto.setCnpj("12345678000190");
        dto.setTradeName("Solar XPTO");
        dto.setCorporateName("Solar XPTO Ltda");
        return dto;
    }

    private void stubCommonBootstrap(User creator) {
        when(currentUserService.getCurrentUser()).thenReturn(creator);
        Position admin = new Position();
        admin.setId(UUID.randomUUID());
        admin.setName(Position.ADMIN_NAME);
        when(positionRepository.findByName(Position.ADMIN_NAME)).thenReturn(Optional.of(admin));
        when(companyRepository.existsByCnpj(any())).thenReturn(false);
        when(companyRepository.existsBySlug(any())).thenReturn(false);
        when(companyRepository.save(any())).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            company.setId(UUID.randomUUID());
            return company;
        });
    }

    @Test
    void savingASupplierCompanySubscribesItToTheDefaultPlan() {
        User creator = new User();
        creator.setId(UUID.randomUUID());
        stubCommonBootstrap(creator);

        Supplier savedSupplier = new Supplier();
        savedSupplier.setId(UUID.randomUUID());
        when(supplierRepository.save(any())).thenReturn(savedSupplier);

        CompanyPlans defaultPlan = new CompanyPlans();
        defaultPlan.setId(UUID.randomUUID());
        when(companyPlansRepository.findByName("Gratuito")).thenReturn(Optional.of(defaultPlan));

        service.save(supplierDto());

        verify(subscriptionRepository).save(any());
    }

    @Test
    void savingASupplierCompanyFailsClearlyWhenNoDefaultPlanIsConfigured() {
        User creator = new User();
        creator.setId(UUID.randomUUID());
        stubCommonBootstrap(creator);

        Supplier savedSupplier = new Supplier();
        savedSupplier.setId(UUID.randomUUID());
        when(supplierRepository.save(any())).thenReturn(savedSupplier);
        when(companyPlansRepository.findByName("Gratuito")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.save(supplierDto()));
    }

    @Test
    void savingADemandantCompanyDoesNotCreateASubscription() {
        User creator = new User();
        creator.setId(UUID.randomUUID());
        stubCommonBootstrap(creator);

        CompanyRequestDTO dto = supplierDto();
        dto.setType(CompanyType.DEMANDANT);

        service.save(dto);

        verify(subscriptionRepository, never()).save(any());
        verify(companyPlansRepository, never()).findByName(any());
    }
}
