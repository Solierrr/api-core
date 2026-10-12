package com.solaria.persistence.service.execution;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.solaria.persistence.dto.request.execution.TechnicalServiceRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalServiceResponseDTO;
import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceResponseDTO;
import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.ServiceTransitionConflictException;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.mobile.TechnicalServiceMobileAccess;
import com.solaria.persistence.security.mobile.TechnicalServiceMobileAccess.ActionMode;
import com.solaria.persistence.security.mobile.TechnicalServiceMobileAccess.ServiceAction;

@Service
public class TechnicalServiceService {

    private final TechnicalServiceRepository technicalServiceRepository;
    private final TechnicalProjectRepository technicalProjectRepository;
    private final RbacAuthorizationService rbac;
    private final CurrentUserService currentUserService;
    private final TechnicalServiceMobileAccess mobileAccess;
    private final PersonalTechnicalServiceAssembler personalAssembler;

    public TechnicalServiceService(TechnicalServiceRepository technicalServiceRepository,
                                   TechnicalProjectRepository technicalProjectRepository,
                                   RbacAuthorizationService rbac,
                                   CurrentUserService currentUserService,
                                   TechnicalServiceMobileAccess mobileAccess,
                                   PersonalTechnicalServiceAssembler personalAssembler) {
        this.technicalServiceRepository = technicalServiceRepository;
        this.technicalProjectRepository = technicalProjectRepository;
        this.rbac = rbac;
        this.currentUserService = currentUserService;
        this.mobileAccess = mobileAccess;
        this.personalAssembler = personalAssembler;
    }

    @Transactional
    public TechnicalServiceResponseDTO save(TechnicalServiceRequestDTO dto) {
        TechnicalProject technicalProject = technicalProjectRepository.findForManagementById(dto.getTechnicalProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projeto Técnico não encontrado com ID: " + dto.getTechnicalProjectId()));

        requireProjectOwner(technicalProject);
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
        TechnicalService technicalService = technicalServiceRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico com id:" + id + " não encontrado para agendamento"));
        requireOwner(technicalService);

        if (technicalService.getStatus() != ServiceStatus.OPEN
                && technicalService.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Serviço não pode ser reagendado no status: " + technicalService.getStatus());
        }

        technicalService.setScheduledDate(scheduledDate);
        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional
    public TechnicalServiceResponseDTO updatePurpose(UUID id, String purpose) {
        TechnicalService technicalService = technicalServiceRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico com id:" + id + " não encontrado para atualização"));
        requireOwner(technicalService);

        if (technicalService.getStatus() != ServiceStatus.OPEN) {
            throw new BusinessRuleException(
                    "Finalidade não pode ser alterada: serviço em estado imutável (" + technicalService.getStatus() + ")");
        }

        technicalService.setPurpose(purpose);

        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional
    public PersonalTechnicalServiceResponseDTO accept(UUID id, UUID ignoredAcceptedBy) {
        UUID userId = currentUserService.getProvisionedCurrentUser().getId();
        TechnicalService technicalService = technicalServiceRepository.findForMobileById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico não encontrado com ID: " + id));

        var crew = personalAssembler.loadCrew(List.of(technicalService));
        var accessFacts = mobileAccess.evaluateOne(technicalService, userId, crew);
        ActionMode mode = mobileAccess.authorizeAction(accessFacts, ServiceAction.ACCEPT);
        requireTransition(technicalService, ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS);

        if (mode == ActionMode.REQUESTER && crew.isEmpty()) {
            throw new BusinessRuleException("Serviço requer ao menos um executor antes do aceite");
        }

        technicalService.setStatus(ServiceStatus.IN_PROGRESS);
        technicalService.setAcceptedBy(userId);
        technicalService.setAcceptedAt(OffsetDateTime.now());

        return personalAssembler.assembleOne(technicalServiceRepository.save(technicalService), accessFacts, crew);
    }

    @Transactional
    public PersonalTechnicalServiceResponseDTO complete(UUID id) {
        UUID userId = currentUserService.getProvisionedCurrentUser().getId();
        TechnicalService technicalService = technicalServiceRepository.findForMobileById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico não encontrado com ID: " + id));

        var crew = personalAssembler.loadCrew(List.of(technicalService));
        var accessFacts = mobileAccess.evaluateOne(technicalService, userId, crew);
        mobileAccess.authorizeAction(accessFacts, ServiceAction.COMPLETE);
        requireTransition(technicalService, ServiceStatus.IN_PROGRESS, ServiceStatus.COMPLETED);

        technicalService.setStatus(ServiceStatus.COMPLETED);
        technicalService.setEndDate(OffsetDateTime.now());

        return personalAssembler.assembleOne(technicalServiceRepository.save(technicalService), accessFacts, crew);
    }

    private void requireTransition(TechnicalService service, ServiceStatus expected, ServiceStatus target) {
        if (service.getStatus() != expected) {
            String message = "Transição de status inválida: " + service.getStatus() + " → " + target;
            throw new ServiceTransitionConflictException(message);
        }
    }

    @Transactional
    public TechnicalServiceResponseDTO cancel(UUID id) {
        TechnicalService technicalService = technicalServiceRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico não encontrado com ID: " + id));

        requireOwner(technicalService);

        if (technicalService.getStatus() != ServiceStatus.OPEN && technicalService.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + technicalService.getStatus() + " → CANCELED");
        }

        technicalService.setStatus(ServiceStatus.CANCELED);

        return toResponse(technicalServiceRepository.save(technicalService));
    }

    @Transactional(readOnly = true)
    public TechnicalServiceResponseDTO findById(UUID id) {
        TechnicalService technicalService = technicalServiceRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico não encontrado com ID: " + id));
        requireOwner(technicalService);
        return toResponse(technicalService);
    }

    @Transactional(readOnly = true)
    public TechnicalServiceResponseDTO findById(UUID id, UUID companyId) {
        TechnicalService technicalService = technicalServiceRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço Técnico não encontrado com ID: " + id));
        requireOwner(technicalService);
        rbac.requireOwnCompany(companyId);
        return toResponse(technicalService);
    }

    @Transactional(readOnly = true)
    public List<TechnicalServiceResponseDTO> findByTechnicalProject(UUID technicalProjectId) {
        TechnicalProject project = technicalProjectRepository.findForManagementById(technicalProjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto Técnico não encontrado com ID: " + technicalProjectId));
        requireProjectOwner(project);
        return technicalServiceRepository.findByTechnicalProjectId(technicalProjectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicalServiceResponseDTO> findAllByCompany(UUID companyId) {
        rbac.requireOwnCompany(companyId);
        return technicalServiceRepository.findByTechnicalProject_Requester_Company_Id(rbac.getCurrentCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private void requireOwner(TechnicalService service) {
        requireProjectOwner(service.getTechnicalProject());
    }

    private void requireProjectOwner(TechnicalProject project) {
        rbac.requireOwnCompany(project.getRequester() == null ? null : project.getRequester().getCompany().getId());
    }

    private TechnicalServiceResponseDTO toResponse(TechnicalService entity) {
        TechnicalServiceResponseDTO response = new TechnicalServiceResponseDTO();
        response.setId(entity.getId());
        response.setPurpose(entity.getPurpose());
        response.setStatus(entity.getStatus());
        response.setScheduledDate(entity.getScheduledDate());
        response.setCreatedAt(entity.getCreatedAt());
        response.setAcceptedBy(entity.getAcceptedBy());
        response.setAcceptedAt(entity.getAcceptedAt());
        response.setTechnicalProjectId(entity.getTechnicalProject().getId());
        response.setEndDate(entity.getEndDate());
        return response;
    }
}
