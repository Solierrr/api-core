package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.dto.request.execution.*;
import com.solaria.persistence.dto.response.execution.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.PersonRepository;
import com.solaria.persistence.repository.professional.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectActivityService {
    private final ProjectService projects;
    private final ProjectAccess access;
    private final ProjectActivityRepository activities;
    private final ProjectMemberRepository members;
    private final TechnicalServiceRepository services;
    private final ServiceExecutorRepository executors;
    private final PersonRepository persons;
    private final TechnicianRepository technicians;
    private final TechnicianAffiliationRepository affiliations;
    private final com.solaria.persistence.repository.identity.UserRepository users;
    private final com.solaria.persistence.repository.professional.ProfessionalOnboardingRepository
            profiles;

    public List<ProjectActivityResponse> list(UUID id) {
        access.read(projects.entity(id, false));
        var rows = new ArrayList<>(activities.findByProjectIdOrderByStartsAt(id));
        var linked =
                rows.stream()
                        .map(ProjectActivity::getTechnicalServiceId)
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet());
        services.findByTechnicalProjectId(id).stream()
                .filter(service -> !linked.contains(service.getId()))
                .map(this::legacyActivity)
                .forEach(rows::add);
        return rows.stream()
                .sorted(java.util.Comparator.comparing(ProjectActivity::getStartsAt))
                .map(this::response)
                .toList();
    }

    public ProjectActivityResponse save(UUID projectId, UUID activityId, ProjectActivityRequest d) {
        var p = projects.entity(projectId, true);
        access.manage(p);
        access.mutable(p);
        if (!d.endsAt().isAfter(d.startsAt()))
            throw new BusinessRuleException("O término precisa ser posterior ao início");
        if (d.assigneeIds().stream().distinct().count() != d.assigneeIds().size())
            throw new BusinessRuleException("Responsáveis repetidos");
        for (UUID userId : d.assigneeIds()) {
            requireActiveUser(userId);
            var m =
                    members.findByProjectIdAndUserId(projectId, userId)
                            .filter(ProjectMember::isActive)
                            .orElseThrow(
                                    () ->
                                            new BusinessRuleException(
                                                    "Responsável não participa do projeto"));
            if (m.getRole() == ProjectRole.VIEWER)
                throw new BusinessRuleException("Observador não pode executar atividades");
        }
        var a = activityId == null ? new ProjectActivity() : activity(projectId, activityId);
        if (a.getStatus() != ServiceStatus.OPEN)
            throw new BusinessRuleException("Somente atividades planejadas podem ser editadas");
        if (activityId != null && a.getType() != d.type())
            throw new BusinessRuleException("Tipo de atividade é imutável; cancele e crie outra");
        a.setProjectId(projectId);
        a.setTitle(d.title().trim());
        a.setDescription(d.description());
        a.setType(d.type());
        a.setStartsAt(d.startsAt());
        a.setEndsAt(d.endsAt());
        a.setOnsite(d.onsite());
        a.setAssigneeIds(new ArrayList<>(d.assigneeIds()));
        if (d.type() == ProjectActivityType.SERVICE) {
            var s =
                    a.getTechnicalServiceId() == null
                            ? new TechnicalService()
                            : services.findById(a.getTechnicalServiceId()).orElseThrow();
            s.setTechnicalProject(p);
            s.setPurpose(a.getTitle());
            s.setScheduledDate(a.getStartsAt());
            if (s.getCreatedAt() == null) s.setCreatedAt(OffsetDateTime.now());
            services.saveAndFlush(s);
            a.setTechnicalServiceId(s.getId());
            if (activityId != null) executors.deleteAll(executors.findByServiceId(s.getId()));
            for (UUID uid : d.assigneeIds().stream().sorted().toList()) {
                var t = requireApproved(uid);
                // Approval permits independent execution without creating company employment.
                technicians.findByIdForUpdate(t.getId()).orElseThrow();
                var active =
                        affiliations.findByTechnicianId(t.getId()).stream()
                                .filter(f -> Boolean.TRUE.equals(f.getActive()))
                                .toList();
                var affiliation =
                        active.stream()
                                .filter(
                                        f ->
                                                f.getCompany() != null
                                                        && f.getCompany()
                                                                .getId()
                                                                .equals(p.getOwnerCompanyId()))
                                .findFirst()
                                .orElseGet(
                                        () ->
                                                active.stream()
                                                        .filter(
                                                                f ->
                                                                        f.getAffiliationType()
                                                                                == com.solaria
                                                                                        .persistence
                                                                                        .domain
                                                                                        .enums
                                                                                        .professional
                                                                                        .TechnicalAffiliationType
                                                                                        .INDEPENDENT)
                                                        .findFirst()
                                                        .orElse(null));
                if (affiliation == null && active.size() == 1) affiliation = active.getFirst();
                if (affiliation == null && active.size() > 1)
                    throw new BusinessRuleException(
                            "Profissional possui afiliações ambíguas; defina sua atuação antes de"
                                    + " atribuir o serviço");
                if (affiliation == null) {
                    affiliation =
                            new com.solaria.persistence.domain.entity.professional
                                    .TechnicianAffiliation();
                    affiliation.setTechnician(t);
                    affiliation.setAffiliationType(
                            com.solaria.persistence.domain.enums.professional
                                    .TechnicalAffiliationType.INDEPENDENT);
                    affiliation.setActive(true);
                    affiliation = affiliations.saveAndFlush(affiliation);
                }
                var e = new ServiceExecutor();
                e.setService(s);
                e.setTechnicianAffiliation(affiliation);
                e.setFunction(a.getTitle());
                executors.save(e);
            }
        }
        activities.save(a);
        projects.record(
                p, activityId == null ? "ACTIVITY_CREATED" : "ACTIVITY_UPDATED", a.getTitle());
        return response(a);
    }

    public ProjectActivityResponse status(UUID projectId, UUID id, ServiceStatus next) {
        var p = projects.entity(projectId, true);
        access.contribute(p);
        var a = activity(projectId, id);
        var role = access.read(p);
        if (role == ProjectRole.MEMBER && !a.getAssigneeIds().contains(access.user().getId()))
            throw new UnauthorizedAccessException("Atividade atribuída a outro participante");
        if (next != ServiceStatus.IN_PROGRESS
                && next != ServiceStatus.COMPLETED
                && next != ServiceStatus.CANCELED)
            throw new BusinessRuleException("Situação inválida");
        if ((a.getStatus() == ServiceStatus.COMPLETED || a.getStatus() == ServiceStatus.CANCELED)
                || (next == ServiceStatus.COMPLETED && a.getStatus() != ServiceStatus.IN_PROGRESS))
            throw new BusinessRuleException("Transição inválida da atividade");
        if (next == ServiceStatus.IN_PROGRESS) {
            if (a.getStatus() != ServiceStatus.OPEN)
                throw new BusinessRuleException("Atividade já iniciada");
            if (a.getEndsAt() == null)
                throw new BusinessRuleException(
                        "Defina o término previsto da atividade na agenda antes de iniciar");
            if (a.getAssigneeIds().isEmpty())
                throw new BusinessRuleException("Defina ao menos um responsável");
            if (a.isOnsite() && (p.getLocation() == null || !p.getLocation().isUsable()))
                throw new BusinessRuleException(
                        "Preencha o local antes de iniciar uma atividade presencial");
            for (var uid : a.getAssigneeIds()) {
                requireActiveUser(uid);
                if (a.getType() == ProjectActivityType.SERVICE) requireApproved(uid);
                if (members.findByProjectIdAndUserId(projectId, uid)
                        .filter(m -> m.isActive() && m.getRole() != ProjectRole.VIEWER)
                        .isEmpty())
                    throw new BusinessRuleException("Responsável removido; atualize a atividade");
            }
            if (a.getTechnicalServiceId() != null
                    && executors.findByServiceId(a.getTechnicalServiceId()).stream()
                            .anyMatch(
                                    e ->
                                            !Boolean.TRUE.equals(
                                                    e.getTechnicianAffiliation().getActive())))
                throw new BusinessRuleException(
                        "Afiliação inativa; atualize os responsáveis do serviço");
        }
        a.setStatus(next);
        if (a.getTechnicalServiceId() != null) {
            var s = services.findById(a.getTechnicalServiceId()).orElseThrow();
            s.setStatus(next);
            if (next == ServiceStatus.IN_PROGRESS) {
                s.setAcceptedBy(access.user().getId());
                s.setAcceptedAt(OffsetDateTime.now());
            }
            if (next == ServiceStatus.COMPLETED) s.setEndDate(OffsetDateTime.now());
            services.save(s);
        }
        projects.record(p, "ACTIVITY_STATUS", a.getTitle() + " → " + next);
        return response(activities.save(a));
    }

    private com.solaria.persistence.domain.entity.identity.User requireActiveUser(UUID id) {
        return users.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .orElseThrow(
                        () ->
                                new BusinessRuleException(
                                        "Responsável com conta inativa ou indisponível"));
    }

    private com.solaria.persistence.domain.entity.professional.Technician requireApproved(UUID id) {
        var user = requireActiveUser(id);
        var technician =
                persons.findByUserId(id)
                        .stream().map(person -> technicians.findByPersonId(person.getId())).flatMap(java.util.Optional::stream).findFirst()
                        .orElseThrow(
                                () ->
                                        new BusinessRuleException(
                                                "Serviços exigem responsáveis profissionais"));
        boolean valid =
                technician.getStatus()
                                == com.solaria.persistence.domain.enums.professional
                                        .TechnicianStatus.APPROVED
                        && profiles.findByAuthId(user.getAuthId())
                                .map(p -> p.getApprovedVersion() > 0 && !p.isSuspended())
                                .orElse(true);
        if (!valid)
            throw new BusinessRuleException(
                    "Profissional ainda não aprovado ou com atuação suspensa");
        return technician;
    }

    private ProjectActivity activity(UUID projectId, UUID id) {
        return activities
                .findById(id)
                .filter(a -> projectId.equals(a.getProjectId()))
                .orElseGet(
                        () -> {
                            var service =
                                    services.findById(id)
                                            .filter(
                                                    s ->
                                                            projectId.equals(
                                                                    s.getTechnicalProject()
                                                                            .getId()))
                                            .orElseThrow(
                                                    () ->
                                                            new ResourceNotFoundException(
                                                                    "Atividade não encontrada"));
                            return legacyActivity(service);
                        });
    }

    private ProjectActivity legacyActivity(TechnicalService s) {
        var a = new ProjectActivity();
        a.setId(s.getId());
        a.setProjectId(s.getTechnicalProject().getId());
        a.setTechnicalServiceId(s.getId());
        a.setType(ProjectActivityType.SERVICE);
        a.setStatus(s.getStatus());
        a.setTitle(s.getPurpose().substring(0, Math.min(160, s.getPurpose().length())));
        if (s.getPurpose().length() > 160) a.setDescription(s.getPurpose());
        a.setStartsAt(s.getScheduledDate() == null ? s.getCreatedAt() : s.getScheduledDate());
        a.setEndsAt(
                s.getEndDate() != null && s.getEndDate().isAfter(a.getStartsAt())
                        ? s.getEndDate()
                        : null);
        a.setAssigneeIds(
                executors.findByServiceId(s.getId()).stream()
                        .map(
                                e ->
                                        e.getTechnicianAffiliation()
                                                .getTechnician()
                                                .getPerson()
                                                .getUser())
                        .filter(Objects::nonNull)
                        .map(u -> u.getId())
                        .distinct()
                        .toList());
        return a;
    }

    private ProjectActivityResponse response(ProjectActivity a) {
        return new ProjectActivityResponse(
                a.getId(),
                a.getProjectId(),
                a.getTitle(),
                a.getDescription(),
                a.getType(),
                a.getStatus(),
                a.getStartsAt(),
                a.getEndsAt(),
                a.isOnsite(),
                a.getAssigneeIds(),
                a.getTechnicalServiceId());
    }
}
