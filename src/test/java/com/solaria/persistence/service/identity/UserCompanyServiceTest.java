package com.solaria.persistence.service.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.Permission;
import com.solaria.persistence.domain.entity.identity.Position;
import com.solaria.persistence.domain.entity.identity.PositionPermission;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserCompany;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.response.identity.MyMembershipResponseDTO;
import com.solaria.persistence.dto.response.identity.OperationalContextResponseDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;
import com.solaria.persistence.repository.company.CompanyPositionsRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.identity.PositionPermissionRepository;
import com.solaria.persistence.repository.identity.PositionRepository;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@ExtendWith(MockitoExtension.class)
class UserCompanyServiceTest {

    @Mock private UserCompanyRepository userCompanyRepository;
    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private CompanyPositionsRepository companyPositionsRepository;
    @Mock private RbacAuthorizationService rbac;
    @Mock private PositionPermissionRepository positionPermissionRepository;
    @Mock private CurrentUserService currentUserService;

    private UserCompanyService service;

    private UUID userId;
    private Company company;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserCompanyService(userCompanyRepository, userRepository, companyRepository,
                positionRepository, companyPositionsRepository, rbac, positionPermissionRepository,
                currentUserService);
        userId = UUID.randomUUID();
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setType(CompanyType.SUPPLIER);
        company.setStatus(CompanyStatus.APPROVED);
        user = new User();
        user.setId(userId);
    }

    private UserCompany membership(String positionName) {
        Position position = new Position();
        position.setId(UUID.randomUUID());
        position.setName(positionName);
        UserCompany membership = new UserCompany();
        membership.setId(UUID.randomUUID());
        membership.setUser(user);
        membership.setCompany(company);
        membership.setPosition(position);
        return membership;
    }

    private PositionPermission grant(String permissionName) {
        Permission permission = new Permission();
        permission.setPermissionName(permissionName);
        PositionPermission grant = new PositionPermission();
        grant.setPermission(permission);
        return grant;
    }

    @Test
    void myMembershipsListsTheCompanyPositionAndSortedPermissions() {
        UserCompany membership = membership("VENDEDOR");
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(userId));
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.of(membership));
        when(positionPermissionRepository.findByPositionId(membership.getPosition().getId()))
                .thenReturn(List.of(grant("POST /api/offers"), grant("GET /api/offers/supplier/{supplierId}")));

        List<MyMembershipResponseDTO> result = service.findMyMemberships();

        assertEquals(1, result.size());
        assertEquals(company.getId(), result.get(0).companyId());
        assertEquals("VENDEDOR", result.get(0).position().name());
        assertEquals(membership.getPosition().getId(), result.get(0).position().id());
        assertEquals(List.of("GET /api/offers/supplier/{supplierId}", "POST /api/offers"),
                result.get(0).permissions());
    }

    @Test
    void adminComesWithoutPermissionsBecauseItPassesEveryEndpoint() {
        UserCompany membership = membership(Position.ADMIN_NAME);
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(userId));
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.of(membership));

        List<MyMembershipResponseDTO> result = service.findMyMemberships();

        assertTrue(result.get(0).permissions().isEmpty());
        verifyNoInteractions(positionPermissionRepository);
    }

    @Test
    void myMembershipsIsEmptyWithoutACompanyOrWithoutAUser() {
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(userId));
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertTrue(service.findMyMemberships().isEmpty());

        when(currentUserService.findCurrentUserId()).thenReturn(Optional.empty());
        assertTrue(service.findMyMemberships().isEmpty());
    }

    @Test
    void myContextCarriesCompanyTypeStatusPositionAndAdminFlag() {
        UserCompany membership = membership(Position.ADMIN_NAME);
        company.setStatus(CompanyStatus.UNDER_ANALYSIS);
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(userId));
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.of(membership));

        OperationalContextResponseDTO context = service.findMyContext();

        assertEquals(company.getId(), context.companyId());
        assertEquals(CompanyType.SUPPLIER, context.companyType());
        assertEquals(CompanyStatus.UNDER_ANALYSIS, context.companyStatus());
        assertEquals(Position.ADMIN_NAME, context.positionName());
        assertTrue(context.admin());
    }

    @Test
    void myContextForANonAdminIsNotAdminAndKeepsItsPermissions() {
        UserCompany membership = membership("VENDEDOR");
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(userId));
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.of(membership));
        when(positionPermissionRepository.findByPositionId(membership.getPosition().getId()))
                .thenReturn(List.of(grant("GET /api/sales")));

        OperationalContextResponseDTO context = service.findMyContext();

        assertEquals(false, context.admin());
        assertEquals(List.of("GET /api/sales"), context.permissions());
    }

    @Test
    void myContextIsNullWithoutACompany() {
        when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(userId));
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertNull(service.findMyContext());
    }

    @Test
    void findByUserBuildsTheResponseFromTheEntityWithoutFollowingItsRelations() {
        UserCompany membership = membership("VENDEDOR");
        when(userCompanyRepository.findByUserId(userId)).thenReturn(Optional.of(membership));

        List<UserCompanyResponseDTO> result = service.findByUser(userId);

        verify(rbac).requireOwnCompany(company.getId());
        assertEquals(1, result.size());
        UserCompanyResponseDTO dto = result.get(0);
        assertEquals(membership.getId(), dto.getId());
        assertEquals(company.getId(), dto.getCompanyId());
        assertEquals(userId, dto.getUserId());
        assertEquals("VENDEDOR", dto.getPosition().getName());
        assertEquals(membership.getPosition().getId(), dto.getPosition().getId());
        assertEquals(company.getId(), dto.getPosition().getCompanyId());
    }
}
