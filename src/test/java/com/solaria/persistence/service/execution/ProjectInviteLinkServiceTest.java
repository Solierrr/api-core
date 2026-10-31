package com.solaria.persistence.service.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.dto.request.execution.ProjectInviteLinkRequest;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.UserRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ProjectInviteLinkServiceTest {
    @Mock ProjectService projects;
    @Mock ProjectAccess access;
    @Mock ProjectInviteLinkRepository links;
    @Mock ProjectMemberRepository members;
    @Mock UserRepository users;
    ProjectInviteLinkService service;
    User user;
    TechnicalProject project;

    @BeforeEach
    void setup() {
        service = new ProjectInviteLinkService(projects, access, links, members, users, "https://app.test/");
        user = new User();
        user.setId(UUID.randomUUID());
        user.setAuthId(UUID.randomUUID());
        user.setUsername("Guest");
        project = new TechnicalProject();
        project.setId(UUID.randomUUID());
        project.setName("Private project");
        lenient().when(access.user()).thenReturn(user);
        lenient().when(projects.entity(project.getId(), true)).thenReturn(project);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private void session(boolean verified) {
        var token =
                Jwt.withTokenValue("test")
                        .header("alg", "none")
                        .subject(user.getAuthId().toString())
                        .claim("email_verified", verified)
                        .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
    }

    private ProjectInviteLink link(String token) {
        var l = new ProjectInviteLink();
        l.setProjectId(project.getId());
        l.setTokenHash(ProjectInviteLinkService.hash(token));
        l.setRole(ProjectRole.MEMBER);
        l.setExpiresAt(OffsetDateTime.now().plusDays(1));
        l.setCreatedBy(UUID.randomUUID());
        lenient().when(links.findByTokenHash(l.getTokenHash())).thenReturn(Optional.of(l));
        return l;
    }

    @Test
    void creationReturnsTheAddressOnceAndStoresOnlyItsHash() {
        var response =
                service.create(project.getId(), new ProjectInviteLinkRequest(ProjectRole.VIEWER, null, null, null));

        var saved = ArgumentCaptor.forClass(ProjectInviteLink.class);
        verify(links).save(saved.capture());
        String token = response.url().substring("https://app.test/convite/".length());
        assertTrue(response.url().startsWith("https://app.test/convite/"));
        assertEquals(43, token.length());
        assertNotEquals(token, saved.getValue().getTokenHash());
        assertEquals(ProjectInviteLinkService.hash(token), saved.getValue().getTokenHash());
        assertEquals(ProjectRole.VIEWER, saved.getValue().getRole());
        assertTrue(saved.getValue().getExpiresAt().isAfter(OffsetDateTime.now().plusDays(6)));
        verify(projects).record(eq(project), eq("INVITE_LINK_CREATED"), any());
    }

    @Test
    void managerCannotBeInvitedByLink() {
        assertThrows(
                BusinessRuleException.class,
                () -> service.create(project.getId(), new ProjectInviteLinkRequest(ProjectRole.MANAGER, null, 7, null)));
        verify(links, never()).save(any());
    }

    @Test
    void listedLinksNeverCarryTheAddress() {
        var l = link("abc");
        when(links.findByProjectIdOrderByCreatedAtDesc(project.getId())).thenReturn(java.util.List.of(l));
        when(projects.entity(project.getId(), false)).thenReturn(project);

        assertNull(service.list(project.getId()).get(0).url());
    }

    @Test
    void validLinkCreatesAnActiveMemberAndConsumesOneUse() {
        session(true);
        var l = link("good");
        when(members.findByProjectIdAndUserId(project.getId(), user.getId())).thenReturn(Optional.empty());
        when(links.consume(eq(l.getId()), any())).thenReturn(1);

        service.redeem("good");

        verify(members).save(argThat(m -> m.isActive() && m.getUserId().equals(user.getId()) && m.getRole() == ProjectRole.MEMBER));
        verify(projects).record(eq(project), eq("INVITE_LINK_USED"), any());
    }

    @Test
    void unknownRevokedExpiredAndExhaustedLinksAreIndistinguishable() {
        session(true);
        when(links.findByTokenHash(any())).thenReturn(Optional.empty());
        var unknown = assertThrows(ResourceNotFoundException.class, () -> service.redeem("nope"));

        var revoked = link("revoked");
        revoked.setStatus(ProjectInviteLinkStatus.REVOKED);
        var expired = link("expired");
        expired.setExpiresAt(OffsetDateTime.now().minusSeconds(1));
        var exhausted = link("exhausted");
        exhausted.setMaxUses(1);
        exhausted.setUseCount(1);

        for (var token : new String[] {"revoked", "expired", "exhausted"}) {
            var error = assertThrows(ResourceNotFoundException.class, () -> service.redeem(token));
            assertEquals(unknown.getMessage(), error.getMessage());
        }
        verifyNoInteractions(members);
    }

    @Test
    void unverifiedEmailCannotRedeem() {
        session(false);
        assertThrows(BusinessRuleException.class, () -> service.redeem("good"));
        verifyNoInteractions(members);
    }

    @Test
    void currentMemberDoesNotSpendAUse() {
        session(true);
        link("good");
        var member = new ProjectMember();
        member.setActive(true);
        when(members.findByProjectIdAndUserId(project.getId(), user.getId())).thenReturn(Optional.of(member));

        service.redeem("good");

        verify(links, never()).consume(any(), any());
        verify(members, never()).save(any());
    }

    @Test
    void useLimitReachedByAConcurrentRedemptionIsRefused() {
        session(true);
        var l = link("race");
        when(members.findByProjectIdAndUserId(project.getId(), user.getId())).thenReturn(Optional.empty());
        when(links.consume(eq(l.getId()), any())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.redeem("race"));
        verify(members, never()).save(any());
    }

    @Test
    void revokingMarksTheLinkAndRecordsIt() {
        var l = link("abc");
        when(links.findByIdAndProjectId(l.getId(), project.getId())).thenReturn(Optional.of(l));

        service.revoke(project.getId(), l.getId());

        assertEquals(ProjectInviteLinkStatus.REVOKED, l.getStatus());
        assertNotNull(l.getRevokedAt());
        verify(projects).record(eq(project), eq("INVITE_LINK_REVOKED"), any());
    }
}
