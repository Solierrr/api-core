package com.solaria.persistence.service.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.databind.ObjectMapper;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.AccessCode;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.identity.AccessCodeStatus;
import com.solaria.persistence.dto.request.identity.AccessCodeRequestDTO;
import com.solaria.persistence.dto.response.identity.AccessCodeResponseDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.identity.AccessCodeRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@ExtendWith(MockitoExtension.class)
class AccessCodeServiceTest {

    @Mock private AccessCodeRepository accessCodeRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private CompanyPositionsRepository companyPositionsRepository;
    @Mock private UserCompanyRepository userCompanyRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private RbacAuthorizationService rbac;

    private AccessCodeService service;

    private UUID companyId;
    private UUID positionId;
    private Company company;
    private Position position;

    @BeforeEach
    void setUp() {
        service = new AccessCodeService(accessCodeRepository, companyRepository, positionRepository,
                companyPositionsRepository, userCompanyRepository, currentUserService, rbac, new ObjectMapper());

        companyId = UUID.randomUUID();
        positionId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);
        position = new Position();
        position.setId(positionId);
        position.setName("MEMBER");
    }

    @Test
    void generateCreatesAnActiveCodeForACompanyLinkedPosition() {
        AccessCodeRequestDTO dto = new AccessCodeRequestDTO();
        dto.setCompanyId(companyId);
        dto.setPositionId(positionId);

        doNothing().when(rbac).requireOwnCompany(companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
        when(companyPositionsRepository.existsByCompanyIdAndPositionId(companyId, positionId)).thenReturn(true);
        when(accessCodeRepository.existsByCode(any())).thenReturn(false);
        when(accessCodeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AccessCodeResponseDTO response = service.generate(dto);

        assertEquals(AccessCodeStatus.ACTIVE, response.getStatus());
        assertEquals(8, response.getCode().length());
    }

    @Test
    void generateRejectsAPositionNotLinkedToTheCompany() {
        AccessCodeRequestDTO dto = new AccessCodeRequestDTO();
        dto.setCompanyId(companyId);
        dto.setPositionId(positionId);

        doNothing().when(rbac).requireOwnCompany(companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
        when(companyPositionsRepository.existsByCompanyIdAndPositionId(companyId, positionId)).thenReturn(false);

        assertThrows(InvalidFieldException.class, () -> service.generate(dto));
    }

    @Test
    void redeemCreatesTheMembershipAndMarksTheCodeAsUsed() {
        AccessCode accessCode = new AccessCode();
        accessCode.setId(UUID.randomUUID());
        accessCode.setCode("ABC12345");
        accessCode.setCompany(company);
        accessCode.setPosition(position);
        accessCode.setStatus(AccessCodeStatus.ACTIVE);
        accessCode.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        User user = new User();
        user.setId(UUID.randomUUID());

        when(accessCodeRepository.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(userCompanyRepository.existsByUserId(user.getId())).thenReturn(false);
        when(userCompanyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accessCodeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserCompanyResponseDTO response = service.redeem("ABC12345");

        assertEquals(companyId, response.getCompanyId());
        assertEquals(AccessCodeStatus.USED, accessCode.getStatus());
    }

    @Test
    void redeemRejectsAnExpiredCode() {
        AccessCode accessCode = new AccessCode();
        accessCode.setCode("EXPIRED1");
        accessCode.setCompany(company);
        accessCode.setPosition(position);
        accessCode.setStatus(AccessCodeStatus.ACTIVE);
        accessCode.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

        when(accessCodeRepository.findByCode("EXPIRED1")).thenReturn(Optional.of(accessCode));

        assertThrows(BusinessRuleException.class, () -> service.redeem("EXPIRED1"));
    }

    @Test
    void redeemRejectsAnAlreadyUsedCode() {
        AccessCode accessCode = new AccessCode();
        accessCode.setCode("USED1234");
        accessCode.setCompany(company);
        accessCode.setPosition(position);
        accessCode.setStatus(AccessCodeStatus.USED);
        accessCode.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        when(accessCodeRepository.findByCode("USED1234")).thenReturn(Optional.of(accessCode));

        assertThrows(BusinessRuleException.class, () -> service.redeem("USED1234"));
    }

    @Test
    void redeemRejectsAUserThatAlreadyHasACompany() {
        AccessCode accessCode = new AccessCode();
        accessCode.setCode("ABC12345");
        accessCode.setCompany(company);
        accessCode.setPosition(position);
        accessCode.setStatus(AccessCodeStatus.ACTIVE);
        accessCode.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        User user = new User();
        user.setId(UUID.randomUUID());

        when(accessCodeRepository.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(userCompanyRepository.existsByUserId(user.getId())).thenReturn(true);

        assertThrows(UnauthorizedAccessException.class, () -> service.redeem("ABC12345"));
    }

    @Test
    void redeemRejectsAnUnknownCode() {
        when(accessCodeRepository.findByCode("UNKNOWN1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.redeem("UNKNOWN1"));
    }
}
