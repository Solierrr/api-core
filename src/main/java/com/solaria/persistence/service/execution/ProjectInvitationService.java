package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.dto.request.execution.*;
import com.solaria.persistence.dto.response.execution.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.professional.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectInvitationService {
    private final jakarta.persistence.EntityManager entityManager;
    private final ProjectService projects;
    private final ProjectAccess access;
    private final ProjectInvitationRepository invites;
    private final ProjectMemberRepository members;
    private final ProfessionalTeamRepository teams;
    private final ProfessionalTeamMemberRepository teamMembers;

    public List<ProjectInvitationResponse> mine() {
        var u = access.user();
        return invites.findPendingFor(u.getAuth_id(), email()).stream()
                .filter(i -> i.getExpiresAt().isAfter(OffsetDateTime.now()))
                .map(this::response)
                .toList();
    }

    public List<ProjectInvitationResponse> list(UUID id) {
        var p = projects.entity(id, false);
        access.manage(p);
        return invites.findByProjectIdOrderByCreatedAtDesc(id).stream()
                .map(this::response)
                .toList();
    }

    public List<ProjectInvitationResponse> create(UUID id, ProjectInvitationRequest d) {
        var p = projects.entity(id, true);
        access.manage(p);
        access.mutable(p);
        if (d.role() == ProjectRole.OWNER)
            throw new BusinessRuleException("Não é possível transferir a propriedade por convite");
        var result = new ArrayList<ProjectInvitationResponse>();
        if (d.teamId() != null) {
            var team =
                    teams.findById(d.teamId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Equipe não encontrada"));
            access.ownCompany(team.getCompany().getId());
            if (!team.isActive()) throw new BusinessRuleException("Equipe inativa");
            for (var m : teamMembers.findByTeamId(team.getId())) {
                var person = m.getTechnician().getPerson();
                if (person.getUser() == null
                        || person.getContact() == null
                        || person.getContact().getEmail() == null)
                    throw new BusinessRuleException("Integrante da equipe sem contato cadastrado");
                if (person.getUser().getId().equals(access.user().getId())
                        || members.findByProjectIdAndUserId(id, person.getUser().getId())
                                .filter(ProjectMember::isActive)
                                .isPresent()) continue;
                result.add(
                        response(
                                save(
                                        p,
                                        d,
                                        person.getContact().getEmail(),
                                        person.getUser().getAuth_id())));
            }
            if (result.isEmpty()) throw new BusinessRuleException("Equipe sem integrantes");
        } else {
            if (d.email() == null || d.email().isBlank())
                throw new BusinessRuleException("Informe o e-mail do convidado");
            result.add(response(save(p, d, d.email(), null)));
        }
        return result;
    }

    private ProjectInvitation save(
            TechnicalProject p, ProjectInvitationRequest d, String address, UUID authId) {
        String target = address.trim().toLowerCase(Locale.ROOT);
        if (target.equals(email()) || access.user().getAuth_id().equals(authId))
            throw new BusinessRuleException("Você já participa do projeto");
        boolean duplicate =
                invites.findByProjectIdOrderByCreatedAtDesc(p.getId()).stream()
                        .anyMatch(
                                i ->
                                        i.getStatus() == ProjectInvitationStatus.PENDING
                                                && i.getExpiresAt().isAfter(OffsetDateTime.now())
                                                && (i.getEmail().equals(target)
                                                        || (authId != null
                                                                && authId.equals(
                                                                        i.getRecipientAuthId()))));
        if (duplicate)
            throw new BusinessRuleException("Já existe um convite pendente para este participante");
        var i = new ProjectInvitation();
        i.setProjectId(p.getId());
        i.setEmail(target);
        i.setRecipientAuthId(authId);
        i.setRole(d.role());
        i.setFunctionName(d.functionName());
        i.setExpiresAt(OffsetDateTime.now().plusDays(7));
        i.setCreatedBy(access.user().getId());
        i.setLocale(d.locale() == null ? "pt-BR" : d.locale());
        invites.save(i);
        projects.record(p, "INVITED", "Convite enviado para " + target);
        return i;
    }

    public ProjectInvitationResponse respond(UUID inviteId, boolean accept) {
        var i =
                invites.findById(inviteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Convite indisponível"));
        var p = projects.entity(i.getProjectId(), true);
        entityManager.refresh(i);
        var u = access.user();
        boolean recipient =
                i.getRecipientAuthId() != null
                        ? u.getAuth_id().equals(i.getRecipientAuthId())
                        : i.getEmail().equals(email());
        if (!recipient) throw new UnauthorizedAccessException("Convite indisponível");
        if (i.getStatus() != ProjectInvitationStatus.PENDING
                || !i.getExpiresAt().isAfter(OffsetDateTime.now()))
            throw new BusinessRuleException("Convite expirado ou já respondido");
        if (!(SecurityContextHolder.getContext().getAuthentication()
                        instanceof JwtAuthenticationToken jwt)
                || !Boolean.TRUE.equals(jwt.getToken().getClaimAsBoolean("email_verified")))
            throw new BusinessRuleException(
                    "Verifique o e-mail da conta e renove sua sessão para responder ao convite");
        access.mutable(p);
        i.setStatus(accept ? ProjectInvitationStatus.ACCEPTED : ProjectInvitationStatus.DECLINED);
        i.setRespondedAt(OffsetDateTime.now());
        if (accept) {
            var m =
                    members.findByProjectIdAndUserId(p.getId(), u.getId())
                            .orElseGet(ProjectMember::new);
            if (m.isActive() && m.getProjectId() != null)
                throw new BusinessRuleException("Você já participa do projeto");
            m.setProjectId(p.getId());
            m.setUserId(u.getId());
            m.setRole(i.getRole());
            m.setFunctionName(i.getFunctionName());
            m.setActive(true);
            m.setJoinedAt(OffsetDateTime.now());
            members.save(m);
        }
        projects.record(
                p,
                accept ? "INVITE_ACCEPTED" : "INVITE_DECLINED",
                u.getUsername() + (accept ? " aceitou o convite" : " recusou o convite"));
        return response(invites.save(i));
    }

    public ProjectInvitationResponse revoke(UUID id) {
        var i =
                invites.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));
        var p = projects.entity(i.getProjectId(), true);
        entityManager.refresh(i);
        access.manage(p);
        access.mutable(p);
        if (i.getStatus() != ProjectInvitationStatus.PENDING)
            throw new BusinessRuleException("Convite já respondido");
        i.setStatus(ProjectInvitationStatus.REVOKED);
        projects.record(p, "INVITE_REVOKED", "Convite revogado");
        return response(invites.save(i));
    }

    public ProjectInvitationResponse resend(UUID id) {
        var i =
                invites.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));
        var p = projects.entity(i.getProjectId(), true);
        entityManager.refresh(i);
        access.manage(p);
        access.mutable(p);
        if (i.getStatus() != ProjectInvitationStatus.PENDING)
            throw new BusinessRuleException("Crie um novo convite para este destinatário");
        i.setExpiresAt(OffsetDateTime.now().plusDays(7));
        i.setEmailAttempts(0);
        i.setEmailState("PENDING");
        i.setNextEmailAttemptAt(null);
        projects.record(p, "INVITE_RESENT", "Convite reenviado");
        return response(invites.save(i));
    }

    private String email() {
        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof JwtAuthenticationToken jwt) {
            String e = jwt.getToken().getClaimAsString("email");
            if (e != null) return e.trim().toLowerCase(Locale.ROOT);
        }
        throw new UnauthorizedAccessException("Sessão sem e-mail");
    }

    private ProjectInvitationResponse response(ProjectInvitation i) {
        var p = projects.entity(i.getProjectId(), false);
        return new ProjectInvitationResponse(
                i.getId(),
                i.getProjectId(),
                p.getName(),
                i.getEmail(),
                i.getRole(),
                i.getFunctionName(),
                i.getStatus(),
                i.getExpiresAt(),
                i.getEmailState());
    }
}
