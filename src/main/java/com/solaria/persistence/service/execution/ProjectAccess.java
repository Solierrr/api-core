package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.identity.*;
import com.solaria.persistence.domain.enums.execution.ProjectRole;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.*;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectAccess {
    private final CurrentUserService current;
    private final ProjectMemberRepository members;
    private final UserCompanyRepository companies;
    private final RbacAuthorizationService rbac;

    public User user() {
        var u = current.getCurrentUser();
        if (!Boolean.TRUE.equals(u.getActive()))
            throw new UnauthorizedAccessException("Conta inativa");
        return u;
    }

    public UUID adminCompany(User u) {
        UUID selected;
        try { selected = rbac.getCurrentCompanyId(); } catch (UnauthorizedAccessException error) { selected = null; }
        final UUID companyId = selected;
        return companies
                .findAllByUserId(u.getId()).stream()
                .filter(link -> companyId == null || companyId.equals(link.getCompany().getId()))
                .findFirst()
                .filter(l -> Position.ADMIN_NAME.equals(l.getPosition().getName()))
                .map(l -> l.getCompany().getId())
                .orElse(null);
    }

    public ProjectRole role(TechnicalProject p, User u) {
        if (!Boolean.TRUE.equals(u.getActive()))
            throw new UnauthorizedAccessException("Projeto indisponível");
        if (isOwner(p, u)) return ProjectRole.OWNER;
        return members.findByProjectIdAndUserId(p.getId(), u.getId())
                .filter(m -> m.isActive())
                .map(m -> m.getRole())
                .orElseThrow(() -> new UnauthorizedAccessException("Projeto indisponível"));
    }

    public boolean isOwner(TechnicalProject p, User u) {
        return u.getId().equals(p.getOwnerUserId())
                || (p.getOwnerCompanyId() != null && companies.findByUserIdAndCompanyId(u.getId(), p.getOwnerCompanyId())
                        .filter(link -> Position.ADMIN_NAME.equals(link.getPosition().getName())).isPresent());
    }

    public boolean canRead(TechnicalProject p) {
        try {
            read(p);
            return true;
        } catch (UnauthorizedAccessException e) {
            return false;
        }
    }

    public ProjectRole read(TechnicalProject p) {
        return role(p, user());
    }

    public void manage(TechnicalProject p) {
        var role = read(p);
        if (role != ProjectRole.OWNER && role != ProjectRole.MANAGER)
            throw new UnauthorizedAccessException("Apenas responsáveis podem gerenciar o projeto");
    }

    public void mutable(TechnicalProject p) {
        if (p.isArchived()
                || p.getStatus()
                        == com.solaria.persistence.domain.enums.execution.ServiceStatus.COMPLETED
                || p.getStatus()
                        == com.solaria.persistence.domain.enums.execution.ServiceStatus.CANCELED)
            throw new BusinessRuleException(
                    "Projeto disponível apenas para consulta; reabra para alterar");
    }

    public void contribute(TechnicalProject p) {
        if (read(p) == ProjectRole.VIEWER)
            throw new UnauthorizedAccessException("Acesso somente para consulta");
        mutable(p);
    }

    public void ownCompany(UUID id) {
        companies
                .findByUserIdAndCompanyId(user().getId(), id)
                .orElseThrow(() -> new UnauthorizedAccessException("Empresa indisponível"));
    }

    public void createCompany(UUID id) {
        var u = user();
        var link =
                companies
                        .findByUserIdAndCompanyId(u.getId(), id)
                        .orElseThrow(() -> new UnauthorizedAccessException("Empresa indisponível"));
        if (link.getCompany().getStatus()
                != com.solaria.persistence.domain.enums.company.CompanyStatus.APPROVED)
            throw new BusinessRuleException("Empresa ainda não aprovada");
        if (!rbac.hasEndpointAccess("POST /api/projects"))
            throw new UnauthorizedAccessException("Sem permissão para criar projetos da empresa");
    }
}
