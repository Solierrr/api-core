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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.shared.AddressRepository;
import com.solaria.persistence.repository.company.BusinessContactRepository;
import com.solaria.persistence.repository.company.CompanyPhotoRepository;
import com.solaria.persistence.repository.company.CompanyPlansRepository;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.company.SubscriptionRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.professional.TechnicianAffiliationRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@ExtendWith(MockitoExtension.class)
class CompanyDecisionTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private BusinessContactRepository businessContactRepository;
    @Mock private UserCompanyRepository userCompanyRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private RequesterRepository requesterRepository;
    @Mock private TechnicianAffiliationRepository technicianAffiliationRepository;
    @Mock private CompanyPositionsRepository companyPositionsRepository;
    @Mock private CompanyPhotoRepository companyPhotoRepository;
    @Mock private CompanyPlansRepository companyPlansRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private PositionRepository positionRepository;
    @Mock private RbacAuthorizationService rbac;

    @InjectMocks private CompanyService service;

    private UUID id;
    private Company company;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        company = new Company();
        company.setId(id);
        company.setTradeName("ACME");
        company.setStatus(CompanyStatus.UNDER_ANALYSIS);
    }

    @Test
    void approveMovesACompanyUnderAnalysisToApproved() {
        when(companyRepository.findByIdForUpdate(id)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenAnswer(call -> call.getArgument(0));

        CompanyResponseDTO response = service.approve(id);

        assertEquals(CompanyStatus.APPROVED, company.getStatus());
        assertEquals(CompanyStatus.APPROVED, response.getStatus());
        verify(companyRepository).save(company);
    }

    @Test
    void rejectMovesACompanyUnderAnalysisToRejected() {
        when(companyRepository.findByIdForUpdate(id)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenAnswer(call -> call.getArgument(0));

        CompanyResponseDTO response = service.reject(id);

        assertEquals(CompanyStatus.REJECTED, company.getStatus());
        assertEquals(CompanyStatus.REJECTED, response.getStatus());
    }

    @Test
    void aCompanyAlreadyDecidedCannotBeDecidedAgain() {
        company.setStatus(CompanyStatus.APPROVED);
        when(companyRepository.findByIdForUpdate(id)).thenReturn(Optional.of(company));

        assertThrows(BusinessRuleException.class, () -> service.reject(id));
        assertThrows(BusinessRuleException.class, () -> service.approve(id));

        assertEquals(CompanyStatus.APPROVED, company.getStatus());
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void decidingAMissingCompanyIsNotFound() {
        when(companyRepository.findByIdForUpdate(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.approve(id));
    }
}
