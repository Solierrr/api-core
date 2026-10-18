package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.dto.request.execution.TechnicalServiceRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalServiceResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.execution.ServiceExecutorRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TechnicalServiceService {

    private final TechnicalServiceRepository technicalServiceRepository;
    private final TechnicalProjectRepository technicalProjectRepository;
    private final ServiceExecutorRepository serviceExecutorRepository;
    private final RbacAuthorizationService rbac;
    private final ObjectMapper objectMapper;
    private final ProjectAccess projectAccess;
    private final com.solaria.persistence.repository.execution.ProjectActivityRepository
            projectActivities;

    public TechnicalServiceService(
            TechnicalServiceRepository technicalServiceRepository,
            TechnicalProjectRepository technicalProjectRepository,
            ServiceExecutorRepository serviceExecutorRepository,
            RbacAuthorizationService rbac,
            ObjectMapper objectMapper,
            ProjectAccess projectAccess,
            com.solaria.persistence.repository.execution.ProjectActivityRepository
                    projectActivities) {
        this.technicalServiceRepository = technicalServiceRepository;
        this.technicalProjectRepository = technicalProjectRepository;
        this.serviceExecutorRepository = serviceExecutorRepository;
        this.rbac = rbac;
        this.objectMapper = objectMapper;
        this.projectAccess = projectAccess;
        this.projectActivities = projectActivities;
    }

    @Transactional
    public TechnicalServiceResponseDTO save(TechnicalServiceRequestDTO dto) {
        TechnicalProject technicalProject =
                technicalProjectRepository
                        .findById(dto.getTechnicalProjectId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Projeto Técnico não encontrado com ID: "
                                                        + dto.getTechnicalProjectId()));

        projectAccess.manage(technicalProject);
        projectAccess.mutable(technicalProject);
        TechnicalService technicalService = new TechnicalService();
        technicalService.setTechnicalProject(technicalProject);
        technicalService.setPurpose(dto.getPurpose());
        technicalService.setStatus(ServiceStatus.OPEN);
        technicalService.setScheduledDate(dto.getScheduledDate());
        technicalService.setCreatedAt(OffsetDateTime.now());

        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional
    public TechnicalServiceResponseDTO reschedule(UUID id, OffsetDateTime scheduledDate) {
        TechnicalService technicalService =
                technicalServiceRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Serviço Técnico com id:"
                                                        + id
                                                        + " não encontrado para agendamento"));

        if (technicalService.getStatus() != ServiceStatus.OPEN
                && technicalService.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Serviço não pode ser reagendado no status: " + technicalService.getStatus());
        }

        projectAccess.manage(technicalService.getTechnicalProject());
        projectAccess.mutable(technicalService.getTechnicalProject());
        if (projectActivities.existsByTechnicalServiceId(technicalService.getId()))
            throw new BusinessRuleException("Gerencie este serviço pela agenda do projeto");
        technicalService.setScheduledDate(scheduledDate);
        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional
    public TechnicalServiceResponseDTO updatePurpose(UUID id, String purpose) {
        TechnicalService technicalService =
                technicalServiceRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Serviço Técnico com id:"
                                                        + id
                                                        + " não encontrado para atualização"));

        if (technicalService.getStatus() != ServiceStatus.OPEN) {
            throw new BusinessRuleException(
                    "Finalidade não pode ser alterada: serviço em estado imutável ("
                            + technicalService.getStatus()
                            + ")");
        }

        projectAccess.manage(technicalService.getTechnicalProject());
        projectAccess.mutable(technicalService.getTechnicalProject());
        if (projectActivities.existsByTechnicalServiceId(technicalService.getId()))
            throw new BusinessRuleException("Gerencie este serviço pela agenda do projeto");
        technicalService.setPurpose(purpose);

        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional
    public TechnicalServiceResponseDTO accept(UUID id, UUID acceptedBy) {
        TechnicalService technicalService =
                technicalServiceRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Serviço Técnico não encontrado com ID: " + id));

        projectAccess.manage(technicalService.getTechnicalProject());

        if (technicalService.getStatus() != ServiceStatus.OPEN) {
            throw new BusinessRuleException(
                    "Transição de status inválida: "
                            + technicalService.getStatus()
                            + " → IN_PROGRESS");
        }

        if (!serviceExecutorRepository.existsByServiceId(id)) {
            throw new BusinessRuleException("Serviço requer ao menos um executor antes do aceite");
        }

        projectAccess.manage(technicalService.getTechnicalProject());
        projectAccess.mutable(technicalService.getTechnicalProject());
        if (projectActivities.existsByTechnicalServiceId(technicalService.getId()))
            throw new BusinessRuleException("Gerencie este serviço pela agenda do projeto");
        technicalService.setStatus(ServiceStatus.IN_PROGRESS);
        technicalService.setAcceptedBy(projectAccess.user().getId());
        technicalService.setAcceptedAt(OffsetDateTime.now());

        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional
    public TechnicalServiceResponseDTO complete(UUID id) {
        TechnicalService technicalService =
                technicalServiceRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Serviço Técnico não encontrado com ID: " + id));

        projectAccess.manage(technicalService.getTechnicalProject());

        if (technicalService.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: "
                            + technicalService.getStatus()
                            + " → COMPLETED");
        }

        projectAccess.manage(technicalService.getTechnicalProject());
        projectAccess.mutable(technicalService.getTechnicalProject());
        if (projectActivities.existsByTechnicalServiceId(technicalService.getId()))
            throw new BusinessRuleException("Gerencie este serviço pela agenda do projeto");
        technicalService.setStatus(ServiceStatus.COMPLETED);
        technicalService.setEndDate(OffsetDateTime.now());

        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional
    public TechnicalServiceResponseDTO cancel(UUID id) {
        TechnicalService technicalService =
                technicalServiceRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Serviço Técnico não encontrado com ID: " + id));

        projectAccess.manage(technicalService.getTechnicalProject());

        if (technicalService.getStatus() != ServiceStatus.OPEN
                && technicalService.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: "
                            + technicalService.getStatus()
                            + " → CANCELED");
        }

        projectAccess.manage(technicalService.getTechnicalProject());
        projectAccess.mutable(technicalService.getTechnicalProject());
        if (projectActivities.existsByTechnicalServiceId(technicalService.getId()))
            throw new BusinessRuleException("Gerencie este serviço pela agenda do projeto");
        technicalService.setStatus(ServiceStatus.CANCELED);

        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional(readOnly = true)
    public TechnicalServiceResponseDTO findById(UUID id) {
        TechnicalService technicalService =
                technicalServiceRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Serviço Técnico não encontrado com ID: " + id));
        projectAccess.read(technicalService.getTechnicalProject());
        return toResponse(technicalService);
    }

    @Transactional(readOnly = true)
    public TechnicalServiceResponseDTO findById(UUID id, UUID companyId) {
        TechnicalService technicalService =
                technicalServiceRepository
                        .findByIdAndTechnicalProject_Requester_Company_Id(id, companyId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Serviço Técnico não encontrado com ID: " + id));
        projectAccess.read(technicalService.getTechnicalProject());
        return toResponse(technicalService);
    }

    @Transactional(readOnly = true)
    public List<TechnicalServiceResponseDTO> findByTechnicalProject(UUID technicalProjectId) {
        projectAccess.read(technicalProjectRepository.findById(technicalProjectId).orElseThrow());
        return technicalServiceRepository.findByTechnicalProjectId(technicalProjectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicalServiceResponseDTO> findAllByCompany(UUID companyId) {
        return technicalServiceRepository
                .findByTechnicalProject_Requester_Company_Id(companyId)
                .stream()
                .filter(service -> projectAccess.canRead(service.getTechnicalProject()))
                .map(this::toResponse)
                .toList();
    }

    private TechnicalServiceResponseDTO toResponse(TechnicalService entity) {
        TechnicalServiceResponseDTO response =
                objectMapper.convertValue(entity, TechnicalServiceResponseDTO.class);
        response.setTechnicalProjectId(entity.getTechnicalProject().getId());
        response.setEndDate(entity.getEndDate());
        return response;
    }
}
