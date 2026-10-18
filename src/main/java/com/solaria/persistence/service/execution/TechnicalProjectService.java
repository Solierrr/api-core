package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.dto.request.execution.TechnicalProjectRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalProjectResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.repository.unit.LocalUnitRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TechnicalProjectService {

    private final TechnicalProjectRepository technicalProjectRepository;
    private final RequesterRepository requesterRepository;
    private final LocalUnitRepository localUnitRepository;
    private final TechnicalServiceRepository technicalServiceRepository;
    private final ProjectAccess projectAccess;
    private final ProjectService workspace;

    public TechnicalProjectService(
            TechnicalProjectRepository technicalProjectRepository,
            RequesterRepository requesterRepository,
            LocalUnitRepository localUnitRepository,
            TechnicalServiceRepository technicalServiceRepository,
            ProjectAccess projectAccess,
            ProjectService workspace) {
        this.technicalProjectRepository = technicalProjectRepository;
        this.requesterRepository = requesterRepository;
        this.localUnitRepository = localUnitRepository;
        this.technicalServiceRepository = technicalServiceRepository;
        this.projectAccess = projectAccess;
        this.workspace = workspace;
    }

    @Transactional
    public TechnicalProjectResponseDTO save(TechnicalProjectRequestDTO dto) {
        var requester = resolveRequester(dto.getRequesterId());
        if (requester == null)
            throw new com.solaria.persistence.exception.BusinessRuleException(
                    "Informe o solicitante ou use /api/projects para projetos profissionais");
        var created =
                workspace.create(
                        new com.solaria.persistence.dto.request.execution.ProjectRequest(
                                "Projeto técnico",
                                null,
                                requester.getCompany().getId(),
                                dto.getLocalUnitId(),
                                null,
                                dto.getStartDate(),
                                dto.getEndDate() == null
                                        ? null
                                        : dto.getEndDate().atOffset(java.time.ZoneOffset.UTC)));
        if (dto.getStatus() != null && dto.getStatus() != ServiceStatus.OPEN)
            workspace.status(created.id(), dto.getStatus());
        return toResponse(technicalProjectRepository.findById(created.id()).orElseThrow());
    }

    @Transactional
    public TechnicalProjectResponseDTO update(UUID id, TechnicalProjectRequestDTO dto) {
        TechnicalProject technicalProject =
                technicalProjectRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Projeto Técnico com id:"
                                                        + id
                                                        + " não encontrado para atualização"));

        projectAccess.manage(technicalProject);
        var requester = resolveRequester(dto.getRequesterId());
        if (requester != null
                && !requester.getCompany().getId().equals(technicalProject.getOwnerCompanyId()))
            throw new com.solaria.persistence.exception.BusinessRuleException(
                    "Propriedade imutável");
        workspace.update(
                id,
                new com.solaria.persistence.dto.request.execution.ProjectRequest(
                        technicalProject.getName(),
                        technicalProject.getDescription(),
                        technicalProject.getOwnerCompanyId(),
                        dto.getLocalUnitId(),
                        technicalProject.getLocation(),
                        dto.getStartDate(),
                        dto.getEndDate() == null
                                ? null
                                : dto.getEndDate().atOffset(java.time.ZoneOffset.UTC)));
        if (dto.getStatus() != null && dto.getStatus() != technicalProject.getStatus())
            workspace.status(id, dto.getStatus());
        projectAccess.read(technicalProject);
        return toResponse(technicalProject);
    }

    @Transactional
    public void deleteById(UUID id) {
        workspace.archive(id);
    }

    @Transactional(readOnly = true)
    public TechnicalProjectResponseDTO findById(UUID id) {
        TechnicalProject technicalProject =
                technicalProjectRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Projeto Técnico não encontrado com ID: " + id));
        projectAccess.read(technicalProject);
        return toResponse(technicalProject);
    }

    @Transactional(readOnly = true)
    public TechnicalProjectResponseDTO findById(UUID id, UUID companyId) {
        TechnicalProject technicalProject =
                technicalProjectRepository
                        .findByIdAndRequester_Company_Id(id, companyId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Projeto Técnico não encontrado com ID: " + id));
        projectAccess.read(technicalProject);
        return toResponse(technicalProject);
    }

    @Transactional(readOnly = true)
    public List<TechnicalProjectResponseDTO> findByRequester(UUID requesterId) {
        return technicalProjectRepository.findByRequesterId(requesterId).stream()
                .filter(projectAccess::canRead)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicalProjectResponseDTO> findAllByCompany(UUID companyId) {
        return technicalProjectRepository.findByRequester_Company_Id(companyId).stream()
                .filter(projectAccess::canRead)
                .map(this::toResponse)
                .toList();
    }

    private Requester resolveRequester(UUID requesterId) {
        if (requesterId == null) {
            return null;
        }
        return requesterRepository
                .findById(requesterId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Solicitante não encontrado com ID: " + requesterId));
    }

    private LocalUnit resolveLocalUnit(UUID localUnitId) {
        if (localUnitId == null) {
            return null;
        }
        return localUnitRepository
                .findById(localUnitId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Unidade Local não encontrada com ID: " + localUnitId));
    }

    private TechnicalProjectResponseDTO toResponse(TechnicalProject entity) {
        TechnicalProjectResponseDTO response = new TechnicalProjectResponseDTO();
        response.setId(entity.getId());
        response.setRequesterId(
                entity.getRequester() == null ? null : entity.getRequester().getId());
        response.setLocalUnitId(
                entity.getLocalUnit() == null ? null : entity.getLocalUnit().getId());
        response.setStatus(entity.getStatus());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        return response;
    }
}
