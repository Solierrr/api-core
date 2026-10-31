package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.dto.request.execution.*;
import com.solaria.persistence.dto.response.execution.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Shareable project invitations. The token has 256 bits of entropy and is stored only as a hash; every invalid state
 * (unknown, revoked, expired, exhausted) answers with the same "unavailable" error so a token cannot be probed.
 */
@Service
@Transactional
public class ProjectInviteLinkService {
    static final int DEFAULT_DAYS = 7;
    private static final String UNAVAILABLE = "Convite indisponível";

    private final SecureRandom random = new SecureRandom();
    private final ProjectService projects;
    private final ProjectAccess access;
    private final ProjectInviteLinkRepository links;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    private final String web;

    public ProjectInviteLinkService(
            ProjectService projects,
            ProjectAccess access,
            ProjectInviteLinkRepository links,
            ProjectMemberRepository members,
            UserRepository users,
            @Value("${app.projects.web-url:http://localhost:5173}") String web) {
        this.projects = projects;
        this.access = access;
        this.links = links;
        this.members = members;
        this.users = users;
        this.web = web.endsWith("/") ? web.substring(0, web.length() - 1) : web;
    }

    @Transactional(readOnly = true)
    public List<ProjectInviteLinkResponse> list(UUID projectId) {
        access.manage(projects.entity(projectId, false));
        return links.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(l -> response(l, null))
                .toList();
    }

    public ProjectInviteLinkResponse create(UUID projectId, ProjectInviteLinkRequest d) {
        var p = projects.entity(projectId, true);
        access.manage(p);
        access.mutable(p);
        if (d.role() != ProjectRole.MEMBER && d.role() != ProjectRole.VIEWER)
            throw new BusinessRuleException("Link de convite aceita apenas Membro ou Observador");
        var token = newToken();
        var l = new ProjectInviteLink();
        l.setProjectId(p.getId());
        l.setTokenHash(hash(token));
        l.setRole(d.role());
        l.setFunctionName(d.functionName());
        l.setMaxUses(d.maxUses());
        l.setExpiresAt(
                OffsetDateTime.now().plusDays(d.expiresInDays() == null ? DEFAULT_DAYS : d.expiresInDays()));
        l.setCreatedBy(access.user().getId());
        links.save(l);
        projects.record(p, "INVITE_LINK_CREATED", "Link de convite criado");
        return response(l, web + "/convite/" + token);
    }

    public void revoke(UUID projectId, UUID linkId) {
        var p = projects.entity(projectId, true);
        access.manage(p);
        var l =
                links.findByIdAndProjectId(linkId, projectId)
                        .orElseThrow(() -> new ResourceNotFoundException("Link não encontrado"));
        if (l.getStatus() == ProjectInviteLinkStatus.REVOKED) return;
        l.setStatus(ProjectInviteLinkStatus.REVOKED);
        l.setRevokedAt(OffsetDateTime.now());
        links.save(l);
        projects.record(p, "INVITE_LINK_REVOKED", "Link de convite desativado");
    }

    @Transactional(readOnly = true)
    public ProjectInviteLinkPreviewResponse preview(String token) {
        access.user();
        var l = usable(token);
        var p = projects.entity(l.getProjectId(), false);
        var by = users.findById(l.getCreatedBy()).map(u -> u.getUsername()).orElse(null);
        return new ProjectInviteLinkPreviewResponse(
                p.getName(), l.getRole(), l.getFunctionName(), by, l.getExpiresAt());
    }

    public ProjectResponse redeem(String token) {
        var u = access.user();
        requireVerifiedEmail();
        var l = usable(token);
        var p = projects.entity(l.getProjectId(), true);
        access.mutable(p);
        var existing = members.findByProjectIdAndUserId(p.getId(), u.getId());
        if (existing.filter(ProjectMember::isActive).isPresent() || access.isOwner(p, u))
            return projects.detail(p.getId());
        if (links.consume(l.getId(), OffsetDateTime.now()) == 0)
            throw new ResourceNotFoundException(UNAVAILABLE);
        var m = existing.orElseGet(ProjectMember::new);
        m.setProjectId(p.getId());
        m.setUserId(u.getId());
        m.setRole(l.getRole());
        m.setFunctionName(l.getFunctionName());
        m.setActive(true);
        m.setJoinedAt(OffsetDateTime.now());
        members.save(m);
        projects.record(p, "INVITE_LINK_USED", u.getUsername() + " entrou pelo link de convite");
        return projects.detail(p.getId());
    }

    private ProjectInviteLink usable(String token) {
        var l =
                links.findByTokenHash(hash(token.trim()))
                        .orElseThrow(() -> new ResourceNotFoundException(UNAVAILABLE));
        boolean exhausted = l.getMaxUses() != null && l.getUseCount() >= l.getMaxUses();
        if (l.getStatus() != ProjectInviteLinkStatus.ACTIVE
                || !l.getExpiresAt().isAfter(OffsetDateTime.now())
                || exhausted) throw new ResourceNotFoundException(UNAVAILABLE);
        return l;
    }

    private void requireVerifiedEmail() {
        if (!(SecurityContextHolder.getContext().getAuthentication()
                        instanceof JwtAuthenticationToken jwt)
                || !Boolean.TRUE.equals(jwt.getToken().getClaimAsBoolean("email_verified")))
            throw new BusinessRuleException(
                    "Verifique o e-mail da conta e renove sua sessão para entrar pelo link");
    }

    private String newToken() {
        var bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private ProjectInviteLinkResponse response(ProjectInviteLink l, String url) {
        return new ProjectInviteLinkResponse(
                l.getId(),
                l.getRole(),
                l.getFunctionName(),
                l.getStatus(),
                l.getExpiresAt(),
                l.getMaxUses(),
                l.getUseCount(),
                url);
    }
}
