package com.solaria.persistence.service.professional;

import java.util.*;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.solaria.persistence.domain.entity.professional.*;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.professional.*;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
@RequiredArgsConstructor
public class ProfessionalTeamService {
    public record Request(@NotNull UUID companyId, @NotBlank @Size(max=100) String name,
            @NotNull UUID responsibleId, @NotNull @Size(min=1,max=100) List<@NotNull UUID> technicianIds, boolean active) {}
    public record Member(UUID id, String name, String slug) {}
    public record Response(UUID id, UUID companyId, String companyName, String companySlug, String name, UUID responsibleId, boolean active, List<Member> members) {}
    private final ProfessionalTeamRepository teams;
    private final ProfessionalTeamMemberRepository members;
    private final TechnicianRepository technicians;
    private final TechnicianAffiliationRepository affiliations;
    private final ProfessionalOnboardingRepository profiles;
    private final CompanyRepository companies;
    private final RbacAuthorizationService rbac;

    @Transactional
    public Response save(UUID id, Request request) {
        var team = id == null ? new ProfessionalTeam() : teams.lockById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada"));
        if (id != null) {
            rbac.requireOwnCompany(team.getCompany().getId());
            if (!team.getCompany().getId().equals(request.companyId())) throw new BusinessRuleException("Empresa da equipe é imutável");
        }
        rbac.requireOwnCompany(request.companyId());
        var company = companies.findById(request.companyId()).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        if (company.getStatus() != CompanyStatus.APPROVED) throw new BusinessRuleException("Empresa não aprovada");
        var ids = new LinkedHashSet<>(request.technicianIds());
        if (ids.size() != request.technicianIds().size() || !ids.contains(request.responsibleId())) {
            throw new BusinessRuleException("Integrantes devem ser únicos e incluir o responsável");
        }
        var selected = ids.stream().map(memberId -> technicians.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"))).toList();
        if (selected.stream().anyMatch(technician -> !eligible(technician, request.companyId()))) {
            throw new BusinessRuleException("Profissionais devem estar aprovados, ativos e vinculados à empresa");
        }
        team.setCompany(company);
        team.setName(request.name().trim());
        team.setResponsible(selected.stream().filter(item -> item.getId().equals(request.responsibleId())).findFirst().orElseThrow());
        team.setActive(request.active());
        teams.saveAndFlush(team);
        if (id != null) { members.deleteByTeamId(id); members.flush(); }
        for (var technician : selected) {
            var member = new ProfessionalTeamMember(); member.setTeam(team); member.setTechnician(technician); members.save(member);
        }
        members.flush();
        return response(team);
    }

    @Transactional(readOnly = true)
    public List<Response> company(UUID companyId) {
        rbac.requireOwnCompany(companyId);
        return teams.findByCompanyId(companyId).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<Response> catalog() {
        return teams.findByActiveTrue().stream()
            .filter(team -> team.getCompany().getStatus() == CompanyStatus.APPROVED && eligible(team.getResponsible(), team.getCompany().getId()))
            .map(this::publicResponse).toList();
    }

    private boolean eligible(Technician technician, UUID companyId) {
        return technician.getStatus() == TechnicianStatus.APPROVED
            && Boolean.TRUE.equals(technician.getPerson().getUser().getActive())
            && profiles.findByTechnicianId(technician.getId()).map(profile -> !profile.isSuspended() && profile.getApprovedVersion() > 0).orElse(true)
            && affiliations.findByTechnicianId(technician.getId()).stream().anyMatch(link -> Boolean.TRUE.equals(link.getActive())
                && link.getCompany() != null && companyId.equals(link.getCompany().getId()));
    }

    private Response publicResponse(ProfessionalTeam team) {
        return response(team, members.findByTeamId(team.getId()).stream().map(ProfessionalTeamMember::getTechnician)
            .filter(technician -> eligible(technician,team.getCompany().getId())).toList());
    }

    private Response response(ProfessionalTeam team) {
        return response(team, members.findByTeamId(team.getId()).stream().map(ProfessionalTeamMember::getTechnician).toList());
    }

    private Response response(ProfessionalTeam team, List<Technician> selected) {
        return new Response(team.getId(), team.getCompany().getId(), team.getCompany().getTradeName(), team.getCompany().getSlug(), team.getName(),
            team.getResponsible().getId(), team.isActive(), selected.stream()
                .map(technician -> new Member(technician.getId(),technician.getPerson().getName(),technician.getSlug())).toList());
    }
}

