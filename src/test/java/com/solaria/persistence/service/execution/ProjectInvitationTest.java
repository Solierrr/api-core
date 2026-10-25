package com.solaria.persistence.service.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.professional.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.*;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationTest {
    @Mock ProjectService projects;
    @Mock ProjectAccess access;
    @Mock ProjectInvitationRepository invites;
    @Mock ProjectMemberRepository members;
    @Mock ProfessionalTeamRepository teams;
    @Mock ProfessionalTeamMemberRepository teamMembers;
    @Mock jakarta.persistence.EntityManager entityManager;
    @InjectMocks ProjectInvitationService service;
    ProjectInvitation invite;
    User user;
    TechnicalProject project;

    void setup(String email, boolean verified) {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setAuthId(UUID.randomUUID());
        user.setUsername("Guest");
        invite = new ProjectInvitation();
        invite.setProjectId(UUID.randomUUID());
        invite.setEmail("guest@solaria.test");
        invite.setExpiresAt(OffsetDateTime.now().plusDays(7));
        project = new TechnicalProject();
        project.setId(invite.getProjectId());
        project.setName("Private project");
        when(invites.findById(invite.getId())).thenReturn(Optional.of(invite));
        when(projects.entity(invite.getProjectId(), true)).thenReturn(project);
        when(access.user()).thenReturn(user);
        var token =
                Jwt.withTokenValue("test")
                        .header("alg", "none")
                        .subject(user.getAuthId().toString())
                        .claim("email", email)
                        .claim("email_verified", verified)
                        .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void guessingInviteIdDoesNotGrantMembership() {
        setup("attacker@solaria.test", true);
        assertThrows(
                UnauthorizedAccessException.class, () -> service.respond(invite.getId(), true));
        verifyNoInteractions(members);
    }

    @Test
    void unverifiedEmailCannotAccept() {
        setup("guest@solaria.test", false);
        assertThrows(BusinessRuleException.class, () -> service.respond(invite.getId(), true));
        verifyNoInteractions(members);
    }

    @Test
    void expiredInviteCannotAccept() {
        setup("guest@solaria.test", true);
        invite.setExpiresAt(OffsetDateTime.now().minusSeconds(1));
        assertThrows(BusinessRuleException.class, () -> service.respond(invite.getId(), true));
        verifyNoInteractions(members);
    }

    @Test
    void teamInviteIsBoundToCanonicalAuthId() {
        setup("guest@solaria.test", true);
        invite.setRecipientAuthId(UUID.randomUUID());
        assertThrows(
                UnauthorizedAccessException.class, () -> service.respond(invite.getId(), true));
        verifyNoInteractions(members);
    }

    @Test
    void acceptingCreatesAnActiveMemberOnce() {
        setup("guest@solaria.test", true);
        when(members.findByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(Optional.empty());
        when(invites.save(invite)).thenReturn(invite);
        when(projects.entity(project.getId(), false)).thenReturn(project);
        service.respond(invite.getId(), true);
        verify(members).save(argThat(m -> m.isActive() && m.getUserId().equals(user.getId())));
        assertThrows(BusinessRuleException.class, () -> service.respond(invite.getId(), true));
        verify(members, times(1)).save(any());
    }
}
