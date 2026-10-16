package com.solaria.persistence.security.rbac;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import java.util.UUID;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.exception.UnauthorizedAccessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class RbacAuthorizationServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserCompanyRepository userCompanyRepository;

    @Mock
    private PositionPermissionRepository positionPermissionRepository;

    private RbacAuthorizationService rbac;

    @BeforeEach
    void setUp() {
        rbac = new RbacAuthorizationService(
                currentUserService, userCompanyRepository, positionPermissionRepository);
    }

    @Test
    void shouldAllowAuthenticatedUserConnectionEndpointsWithoutCompanyRbac() {
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("POST /api/connections"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("GET /api/connections"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("GET /api/connections/{id}"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("DELETE /api/connections/{id}"));

        verifyNoInteractions(currentUserService, userCompanyRepository, positionPermissionRepository);
    }

    @Test
    void companyAdminStillCannotUseEndpointsForTheOtherCompanyType() {
        Company company = new Company();
        company.setType(CompanyType.DEMANDANT);
        Position admin = new Position();
        admin.setName(Position.ADMIN_NAME);
        User user = new User();
        user.setId(UUID.randomUUID());
        UserCompany membership = new UserCompany();
        membership.setCompany(company);
        membership.setPosition(admin);
        when(currentUserService.findCurrentUser()).thenReturn(Optional.of(user));
        when(userCompanyRepository.findByUserId(user.getId())).thenReturn(Optional.of(membership));

        assertFalse(rbac.hasEndpointAccess("POST /api/offers"));
        assertTrue(rbac.hasEndpointAccess("POST /api/local-units"));
        company.setType(CompanyType.SUPPLIER);
        assertTrue(rbac.hasEndpointAccess("POST /api/offers"));
        assertFalse(rbac.hasEndpointAccess("POST /api/local-units"));
    }

    @Test
    void unaffiliatedUserCannotAssignThemselvesToAnExistingCompany() {
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("POST /api/user-companies"));
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("POST /api/company-positions"));
    }
}
