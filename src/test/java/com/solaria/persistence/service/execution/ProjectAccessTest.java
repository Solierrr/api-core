package com.solaria.persistence.service.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.entity.identity.*;
import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.*;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class ProjectAccessTest {
    @Mock CurrentUserService current;
    @Mock ProjectMemberRepository members;
    @Mock UserCompanyRepository companies;
    @Mock RbacAuthorizationService rbac;
    @InjectMocks ProjectAccess access;
    User user;
    TechnicalProject project;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setActive(true);
        project = new TechnicalProject();
        project.setId(UUID.randomUUID());
        project.setOwnerUserId(UUID.randomUUID());
        project.setStatus(ServiceStatus.OPEN);
    }

    @Test
    void personalOwnerDoesNotNeedCompanyMembership() {
        project.setOwnerUserId(user.getId());
        assertEquals(ProjectRole.OWNER, access.role(project, user));
        verifyNoInteractions(members, companies);
    }

    @Test
    void removedMemberCannotRead() {
        when(members.findByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(Optional.empty());
        assertThrows(UnauthorizedAccessException.class, () -> access.role(project, user));
    }

    @Test
    void inactiveAccountCannotReadEvenOwnProject() {
        user.setActive(false);
        project.setOwnerUserId(user.getId());
        assertThrows(UnauthorizedAccessException.class, () -> access.role(project, user));
    }

    @Test
    void viewerCannotContribute() {
        when(current.getCurrentUser()).thenReturn(user);
        var member = new ProjectMember();
        member.setRole(ProjectRole.VIEWER);
        when(members.findByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(Optional.of(member));
        assertThrows(UnauthorizedAccessException.class, () -> access.contribute(project));
    }

    @Test
    void completedCanceledArchivedProjectsAreReadOnly() {
        for (var state : List.of(ServiceStatus.COMPLETED, ServiceStatus.CANCELED)) {
            project.setStatus(state);
            assertThrows(BusinessRuleException.class, () -> access.mutable(project));
        }
        project.setStatus(ServiceStatus.OPEN);
        project.setArchived(true);
        assertThrows(BusinessRuleException.class, () -> access.mutable(project));
    }

    private UserCompany companyLink(boolean admin) {
        var company = new com.solaria.persistence.domain.entity.company.Company();
        company.setId(UUID.randomUUID());
        company.setStatus(com.solaria.persistence.domain.enums.company.CompanyStatus.APPROVED);
        var position = new Position();
        position.setName(admin ? "ADMIN" : "STAFF");
        var link = new UserCompany();
        link.setCompany(company);
        link.setPosition(position);
        link.setUser(user);
        return link;
    }

    @Test
    void companyAdminOwnsOnlyTheirCompanyProjects() {
        var link = companyLink(true);
        project.setOwnerCompanyId(link.getCompany().getId());
        when(companies.findByUserIdAndCompanyId(user.getId(), link.getCompany().getId())).thenReturn(Optional.of(link));
        assertEquals(ProjectRole.OWNER, access.role(project, user));
        project.setOwnerCompanyId(UUID.randomUUID());
        when(members.findByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(Optional.empty());
        assertThrows(UnauthorizedAccessException.class, () -> access.role(project, user));
    }

    @Test
    void employmentAloneDoesNotGrantProjectParticipation() {
        var link = companyLink(false);
        project.setOwnerCompanyId(link.getCompany().getId());
        when(companies.findByUserIdAndCompanyId(user.getId(), link.getCompany().getId())).thenReturn(Optional.of(link));
        when(members.findByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(Optional.empty());
        assertThrows(UnauthorizedAccessException.class, () -> access.role(project, user));
    }

    @Test
    void companyCreationRequiresAnExplicitPermission() {
        var link = companyLink(false);
        when(current.getCurrentUser()).thenReturn(user);
        when(companies.findByUserIdAndCompanyId(user.getId(), link.getCompany().getId())).thenReturn(Optional.of(link));
        assertThrows(
                UnauthorizedAccessException.class,
                () -> access.createCompany(link.getCompany().getId()));
        verify(rbac).hasEndpointAccess("POST /api/projects");
    }
}
