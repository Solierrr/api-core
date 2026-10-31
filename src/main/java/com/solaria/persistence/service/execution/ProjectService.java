package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.*;
import com.solaria.persistence.domain.enums.execution.*;
import com.solaria.persistence.dto.request.execution.*;
import com.solaria.persistence.dto.response.execution.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.execution.*;
import com.solaria.persistence.repository.identity.*;
import com.solaria.persistence.repository.professional.*;
import com.solaria.persistence.repository.unit.LocalUnitRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final TechnicalProjectRepository projects;
    private final ProjectAccess access;
    private final ProjectMemberRepository members;
    private final ProjectHistoryRepository history;
    private final ProjectActivityRepository activities;
    private final TechnicalServiceRepository services;
    private final LocalUnitRepository units;
    private final ProfessionalOnboardingRepository professionals;
    private final PersonRepository persons;
    private final TechnicianRepository technicians;
    private final UserRepository users;
    private final ProfessionalTeamRepository teams;
    private final com.solaria.persistence.repository.company.CompanyRepository companies;
    private final com.solaria.persistence.repository.company.CompanyPhotoRepository companyPhotos;

    public TechnicalProject entity(UUID id, boolean write) {
        return (write ? projects.lockById(id) : projects.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
    }

    public List<ProjectResponse> list(UUID companyId) {
        var user = access.user();
        return projects.findAccessible(user.getId(), companyId, access.adminCompany(user)).stream()
                .map(this::response)
                .toList();
    }

    public Map<String, Object> options(UUID companyId) {
        access.user();
        if (companyId == null) return Map.of("units", List.of(), "teams", List.of());
        access.ownCompany(companyId);
        return Map.of(
                "units",
                units.findByRequester_Company_Id(companyId).stream()
                        .map(
                                u ->
                                        Map.of(
                                                "id",
                                                u.getId(),
                                                "name",
                                                u.getAddress() == null
                                                        ? u.getLocationType().name()
                                                        : u.getAddress().getStreet()
                                                                + ", "
                                                                + u.getAddress().getNumber()
                                                                + " — "
                                                                + u.getAddress().getCity()))
                        .toList(),
                "teams",
                teams.findByCompanyId(companyId).stream()
                        .filter(t -> t.isActive())
                        .map(t -> Map.of("id", t.getId(), "name", t.getName()))
                        .toList());
    }

    public ProjectResponse detail(UUID id) {
        var p = entity(id, false);
        access.read(p);
        return response(p);
    }

    public ProjectResponse create(ProjectRequest d) {
        var user = access.user();
        if (d.ownerCompanyId() != null) access.createCompany(d.ownerCompanyId());
        else {
            boolean approved =
                    professionals
                            .findByAuthId(user.getAuthId())
                            .map(
                                    profile ->
                                            profile.getApprovedVersion() > 0
                                                    && !profile.isSuspended())
                            .orElseGet(
                                    () ->
                                            persons.findByUserId(user.getId())
                                                    .stream().map(person -> technicians.findByPersonId(person.getId())).flatMap(java.util.Optional::stream).findFirst()
                                                    .map(
                                                            t ->
                                                                    t.getStatus()
                                                                            == com.solaria
                                                                                    .persistence
                                                                                    .domain.enums
                                                                                    .professional
                                                                                    .TechnicianStatus
                                                                                    .APPROVED)
                                                    .orElse(false));
            if (!approved)
                throw new BusinessRuleException(
                        "Conclua a aprovação profissional para criar projetos próprios");
        }
        var p = new TechnicalProject();
        p.setStatus(ServiceStatus.OPEN);
        p.setOwnerCompanyId(d.ownerCompanyId());
        p.setOwnerUserId(d.ownerCompanyId() == null ? user.getId() : null);
        p.setCreatedBy(user.getId());
        apply(p, d);
        projects.saveAndFlush(p);
        var member = new ProjectMember();
        member.setProjectId(p.getId());
        member.setUserId(user.getId());
        member.setRole(d.ownerCompanyId() == null ? ProjectRole.OWNER : ProjectRole.MANAGER);
        member.setFunctionName("Responsável");
        members.save(member);
        record(p, "CREATED", "Projeto criado");
        return response(p);
    }

    public ProjectResponse update(UUID id, ProjectRequest d) {
        var p = entity(id, true);
        access.manage(p);
        access.mutable(p);
        if (!Objects.equals(p.getOwnerCompanyId(), d.ownerCompanyId()))
            throw new BusinessRuleException("A propriedade do projeto é imutável");
        apply(p, d);
        record(p, "UPDATED", "Informações atualizadas");
        return response(projects.save(p));
    }

    private void apply(TechnicalProject p, ProjectRequest d) {
        var previousLocation = p.getLocation();
        if (d.startDate() != null && d.endDate() != null && !d.endDate().isAfter(d.startDate()))
            throw new BusinessRuleException("A data final precisa ser posterior ao início");
        p.setName(d.name().trim());
        p.setDescription(d.description());
        p.setStartDate(d.startDate());
        p.setEndDate(d.endDate());
        if (d.localUnitId() != null) {
            var unit =
                    units.findById(d.localUnitId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Unidade não encontrada"));
            if (p.getOwnerCompanyId() == null
                    || !p.getOwnerCompanyId().equals(unit.getRequester().getCompany().getId()))
                throw new UnauthorizedAccessException("Unidade de outra empresa");
            p.setLocalUnit(unit);
            p.setRequester(unit.getRequester());
            var loc = new ProjectLocation();
            if (unit.getAddress() != null) {
                var a = unit.getAddress();
                loc.setState(a.getState());
                loc.setCity(a.getCity());
                loc.setNeighborhood(a.getNeighborhood());
                loc.setZipCode(a.getZipCode());
                loc.setStreet(a.getStreet());
                loc.setNumber(a.getNumber());
            }
            loc.setComplement(unit.getComplement());
            p.setLocation(loc);
        } else {
            p.setLocalUnit(null);
            p.setRequester(null);
            var next = d.location();
            if (next != null) {
                next.setLatitude(null);
                next.setLongitude(null);
                next.setPlaceId(null);
                next.setGeocodedAt(null);
            }
            p.setLocation(next);
        }
        var nextLocation = p.getLocation();
        if (previousLocation != null && nextLocation != null && addressKey(previousLocation).equals(addressKey(nextLocation))) {
            nextLocation.setLatitude(previousLocation.getLatitude());
            nextLocation.setLongitude(previousLocation.getLongitude());
            nextLocation.setPlaceId(previousLocation.getPlaceId());
            nextLocation.setGeocodedAt(previousLocation.getGeocodedAt());
        }
    }

    private List<String> addressKey(ProjectLocation location) {
        return java.util.Arrays.asList(location.getState(), location.getCity(), location.getNeighborhood(),
                location.getStreet(), location.getNumber(), location.getZipCode()).stream()
                .map(value -> value == null ? "" : value.trim().toLowerCase(Locale.ROOT)).toList();
    }

    public ProjectResponse status(UUID id, ServiceStatus next) {
        var p = entity(id, true);
        access.manage(p);
        if (p.isArchived()) throw new BusinessRuleException("Projeto arquivado");
        if (next == null) throw new BusinessRuleException("Informe a situação");
        if ((next == ServiceStatus.COMPLETED || next == ServiceStatus.CANCELED)
                && (services.existsByTechnicalProjectIdAndStatusIn(
                                id, List.of(ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS))
                        || activities.existsByProjectIdAndStatusIn(
                                id, List.of(ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS))))
            throw new BusinessRuleException(
                    "Conclua ou cancele as atividades e serviços pendentes antes de encerrar o"
                            + " projeto");
        var before = p.getStatus();
        p.setStatus(next);
        record(p, "STATUS", before + " → " + next);
        return response(projects.save(p));
    }

    public void archive(UUID id) {
        var p = entity(id, true);
        access.manage(p);
        if (p.isArchived()) return;
        if (services.existsByTechnicalProjectIdAndStatusIn(
                        id, List.of(ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS))
                || activities.existsByProjectIdAndStatusIn(
                        id, List.of(ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS)))
            throw new BusinessRuleException(
                    "Conclua ou cancele atividades pendentes antes de arquivar");
        p.setArchived(true);
        record(p, "ARCHIVED", "Projeto arquivado; histórico preservado");
        projects.save(p);
    }

    public List<ProjectMemberResponse> memberList(UUID id) {
        var p = entity(id, false);
        access.read(p);
        var result = new ArrayList<ProjectMemberResponse>();
        for (var m : members.findByProjectIdAndActiveTrue(id)) {
            var u = users.findById(m.getUserId()).orElseThrow();
            result.add(
                    new ProjectMemberResponse(
                            m.getId(),
                            u.getId(),
                            u.getAuthId(),
                            u.getUsername(),
                            (access.isOwner(p, u) ? ProjectRole.OWNER : m.getRole()),
                            m.getFunctionName()));
        }
        return result;
    }

    public ProjectMemberResponse updateMember(
            UUID id,
            UUID uid,
            com.solaria.persistence.dto.request.execution.ProjectMemberRequest d) {
        var p = entity(id, true);
        access.manage(p);
        access.mutable(p);
        var m =
                members.findByProjectIdAndUserId(id, uid)
                        .filter(ProjectMember::isActive)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Participante não encontrado"));
        if (m.getRole() == ProjectRole.OWNER
                || access.isOwner(p, users.findById(uid).orElseThrow())
                || d.role() == ProjectRole.OWNER)
            throw new BusinessRuleException("Proprietário imutável");
        m.setRole(d.role());
        m.setFunctionName(d.functionName());
        members.save(m);
        record(p, "MEMBER_UPDATED", "Papel do participante atualizado");
        var u = users.findById(uid).orElseThrow();
        return new ProjectMemberResponse(
                m.getId(),
                uid,
                u.getAuthId(),
                u.getUsername(),
                (access.isOwner(p, u) ? ProjectRole.OWNER : m.getRole()),
                m.getFunctionName());
    }

    public void removeMember(UUID id, UUID userId) {
        var p = entity(id, true);
        access.manage(p);
        access.mutable(p);
        var m =
                members.findByProjectIdAndUserId(id, userId)
                        .filter(ProjectMember::isActive)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Participante não encontrado"));
        if (m.getRole() == ProjectRole.OWNER
                || access.isOwner(p, users.findById(userId).orElseThrow()))
            throw new BusinessRuleException(
                    "Responsável original não pode ser removido nesta versão");
        m.setActive(false);
        members.save(m);
        record(
                p,
                "MEMBER_REMOVED",
                "Participante removido: " + users.findById(userId).orElseThrow().getUsername());
    }

    public List<Map<String, Object>> timeline(UUID id) {
        access.read(entity(id, false));
        return history.findByProjectIdOrderByCreatedAtDesc(id).stream()
                .map(
                        h -> {
                            var entry = new LinkedHashMap<String, Object>();
                            entry.put("id", h.getId());
                            entry.put("action", h.getAction());
                            entry.put("detail", h.getDetail());
                            entry.put("createdAt", h.getCreatedAt());
                            entry.put(
                                    "actor",
                                    users.findById(h.getActorId())
                                            .map(u -> u.getUsername())
                                            .orElse("Usuário"));
                            if (h.getReportId() != null) entry.put("reportId", h.getReportId());
                            return (Map<String, Object>) entry;
                        })
                .toList();
    }

    public void record(TechnicalProject p, String action, String detail) {
        record(p, action, detail, null);
    }

    public void record(TechnicalProject p, String action, String detail, UUID reportId) {
        var h = new ProjectHistory();
        h.setReportId(reportId);
        h.setProjectId(p.getId());
        h.setActorId(access.user().getId());
        h.setAction(action);
        h.setDetail(detail);
        history.save(h);
    }

    public ProjectResponse response(TechnicalProject p) {
        var role = access.read(p);
        boolean manager = role == ProjectRole.OWNER || role == ProjectRole.MANAGER;
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getOwnerCompanyId(),
                p.getOwnerUserId(),
                p.getOwnerCompanyId() != null
                        ? companies
                                .findById(p.getOwnerCompanyId())
                                .map(c -> c.getTradeName())
                                .orElse("")
                        : users.findById(p.getOwnerUserId()).map(u -> u.getUsername()).orElse(""),
                p.getStatus(),
                p.isArchived(),
                p.getStartDate(),
                p.getEndDate(),
                p.getLocalUnit() == null ? null : p.getLocalUnit().getId(),
                p.getLocation(),
                role,
                manager,
                !p.isArchived()
                        && p.getStatus() != ServiceStatus.COMPLETED
                        && p.getStatus() != ServiceStatus.CANCELED
                        && role != ProjectRole.VIEWER,
                p.getCreatedAt(),
                ownerCompanyLogoUrl(p));
    }

    private String ownerCompanyLogoUrl(TechnicalProject p) {
        if (p.getOwnerCompanyId() == null) return null;
        return companyPhotos
                .findByCompanyIdAndType(
                        p.getOwnerCompanyId(),
                        com.solaria.persistence.domain.enums.shared.PhotoType.PROFILE)
                .map(photo -> photo.getUrl())
                .orElse(null);
    }
}
