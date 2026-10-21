package com.solaria.persistence.security.rbac;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.solaria.persistence.repository.identity.PlatformAdminRepository;
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

    @Mock
    private PlatformAdminRepository platformAdminRepository;

    private RbacAuthorizationService rbac;

    @BeforeEach
    void setUp() {
        rbac = new RbacAuthorizationService(
                currentUserService, userCompanyRepository, positionPermissionRepository, platformAdminRepository);
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
    void shouldExemptOnlyExplicitSelfProfileAndPhotoMethodPatterns() {
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("GET /api/users/me"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("GET /api/user-photos/user/{userId}"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("PUT /api/user-photos/user/{userId}/type/{type}"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("DELETE /api/user-photos/user/{userId}/type/{type}"));

        verifyNoInteractions(currentUserService, userCompanyRepository, positionPermissionRepository);
    }

    @Test
    void shouldKeepOtherRoutesAndMethodsUnderCompanyRbac() {
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("GET /api/users/{id}"));
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("POST /api/users/me"));
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("GET /api/users/me/settings"));
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("GET /api/me"));
    }
    @Test
    void personalExemptionsAreExactAndPublicUserBootstrapIsGone() {
        for (String resource : new String[]{"persons", "technicians"}) {
            for (String method : new String[]{"POST", "GET"}) {
                assertDoesNotThrow(() -> rbac.requireEndpointAccess(method + " /api/" + resource));
            }
            for (String method : new String[]{"GET", "PUT", "DELETE"}) {
                assertDoesNotThrow(() -> rbac.requireEndpointAccess(method + " /api/" + resource + "/{id}"));
            }
        }
        for (String route : new String[]{"PUT /api/users/{id}", "DELETE /api/users/{id}",
                "POST /api/flux-logs", "GET /api/flux-logs/user/{userId}"}) {
            assertDoesNotThrow(() -> rbac.requireEndpointAccess(route));
        }
        verifyNoInteractions(currentUserService, userCompanyRepository, positionPermissionRepository);
        for (String route : new String[]{"POST /api/users", "PATCH /api/persons/{id}",
                "POST /api/persons/{id}", "GET /api/persons/other", "GET /api/contacts", "GET /api/flux-logs"}) {
            assertThrows(UnauthorizedAccessException.class, () -> rbac.requireEndpointAccess(route));
        }
    }


    @Test
    void qualificationExemptionsAreExactAndCatalogMutationsStayUnderCompanyRbac() {
        for (String resource : new String[]{"professional-registrations", "certification-records"}) {
            assertDoesNotThrow(() -> rbac.requireEndpointAccess("POST /api/" + resource));
            for (String method : new String[]{"GET", "PUT", "DELETE"}) {
                assertDoesNotThrow(() -> rbac.requireEndpointAccess(method + " /api/" + resource + "/{id}"));
            }
        }
        for (String route : new String[]{"GET /api/professional-registrations/technician/{technicianId}",
                "GET /api/certification-records/professional-registration/{professionalRegistrationId}",
                "GET /api/professions", "GET /api/professions/{id}", "GET /api/certifications", "GET /api/certifications/{id}"}) {
            assertDoesNotThrow(() -> rbac.requireEndpointAccess(route));
        }
        verifyNoInteractions(currentUserService, userCompanyRepository, positionPermissionRepository);
        for (String route : new String[]{"GET /api/professional-registrations", "GET /api/certification-records",
                "PATCH /api/professional-registrations/{id}", "POST /api/professions", "PUT /api/certifications/{id}",
                "GET /api/permissions", "GET /api/company-plans"}) {
            assertThrows(UnauthorizedAccessException.class, () -> rbac.requireEndpointAccess(route));
        }
    }
    @Test
    void affiliationExemptionsAreExactAndCorporateReadsStillRequireGrants() {
        for (String route : new String[]{"POST /api/technician-affiliations",
                "PATCH /api/technician-affiliations/{id}/active", "PATCH /api/technician-affiliations/{id}/type",
                "GET /api/technician-affiliations/technician/{technicianId}"}) {
            assertDoesNotThrow(() -> rbac.requireEndpointAccess(route));
        }
        verifyNoInteractions(currentUserService, userCompanyRepository, positionPermissionRepository);
        for (String route : new String[]{"GET /api/technician-affiliations", "PUT /api/technician-affiliations/{id}/active",
                "GET /api/technician-affiliations/company/{companyId}",
                "GET /api/technician-affiliations/{id}/company/{companyId}"}) {
            assertThrows(UnauthorizedAccessException.class, () -> rbac.requireEndpointAccess(route));
        }
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
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(user.getId()));
        when(userCompanyRepository.findByUserId(user.getId())).thenReturn(Optional.of(membership));

        assertFalse(rbac.hasEndpointAccess("POST /api/offers"));
        assertTrue(rbac.hasEndpointAccess("POST /api/local-units"));
        company.setType(CompanyType.SUPPLIER);
        assertTrue(rbac.hasEndpointAccess("POST /api/offers"));
        assertFalse(rbac.hasEndpointAccess("POST /api/local-units"));
    }

    @Test
    void unaffiliatedUserCannotAssignThemselvesToAnExistingCompany() {
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.empty());
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("POST /api/user-companies"));
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("POST /api/company-positions"));
    }

    @Test
    void platformAdminBypassesCompanyAndPositionChecks() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(user.getId()));
        when(platformAdminRepository.existsByUserId(user.getId())).thenReturn(true);

        assertDoesNotThrow(() -> rbac.requireEndpointAccess("POST /api/offers"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("POST /api/local-units"));
    }

    @Test
    void onlyAPlatformAdminCanApproveOrRejectCompaniesAndModels() {
        Company company = new Company();
        company.setType(CompanyType.SUPPLIER);
        Position admin = new Position();
        admin.setName(Position.ADMIN_NAME);
        User companyAdmin = new User();
        companyAdmin.setId(UUID.randomUUID());
        UserCompany membership = new UserCompany();
        membership.setCompany(company);
        membership.setPosition(admin);
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(companyAdmin.getId()));
        when(platformAdminRepository.existsByUserId(companyAdmin.getId())).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("PATCH /api/companies/{id}/approval"));
        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("POST /api/models/{id}/approval"));

        User platformAdmin = new User();
        platformAdmin.setId(UUID.randomUUID());
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(platformAdmin.getId()));
        when(platformAdminRepository.existsByUserId(platformAdmin.getId())).thenReturn(true);

        assertDoesNotThrow(() -> rbac.requireEndpointAccess("PATCH /api/companies/{id}/approval"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("POST /api/models/{id}/approval"));
    }

    @Test
    void nonPlatformAdminWithoutCompanyLinkIsStillRejected() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(user.getId()));
        when(platformAdminRepository.existsByUserId(user.getId())).thenReturn(false);
        when(userCompanyRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedAccessException.class,
                () -> rbac.requireEndpointAccess("POST /api/offers"));
    }
}
