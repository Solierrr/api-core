package com.solaria.persistence.service.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.dto.request.execution.TechnicalProjectRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalProjectResponseDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;

@Service
public class TechnicalProjectService {

    private final TechnicalProjectRepository technicalProjectRepository;
    private final RequesterRepository requesterRepository;
    private final LocalUnitRepository localUnitRepository;
    private final TechnicalServiceRepository technicalServiceRepository;
    private final RbacAuthorizationService rbac;

    public TechnicalProjectService(TechnicalProjectRepository technicalProjectRepository,
                                   RequesterRepository requesterRepository,
                                   LocalUnitRepository localUnitRepository,
                                   TechnicalServiceRepository technicalServiceRepository, RbacAuthorizationService rbac) {
        this.technicalProjectRepository = technicalProjectRepository;
        this.requesterRepository = requesterRepository;
        this.localUnitRepository = localUnitRepository;
        this.technicalServiceRepository = technicalServiceRepository;
        this.rbac = rbac;
    }

    @Transactional
    public TechnicalProjectResponseDTO save(TechnicalProjectRequestDTO dto) {
        Requester requester = resolveRequester(dto.getRequesterId());
        rbac.requireOwnCompany(requester.getCompany().getId());
        LocalUnit unit = resolveLocalUnit(dto.getLocalUnitId());
        validateUnit(requester, unit);
        TechnicalProject technicalProject = new TechnicalProject();
        technicalProject.setRequester(requester);
        technicalProject.setLocalUnit(unit);
        technicalProject.setStatus(dto.getStatus() == null ? ServiceStatus.OPEN : dto.getStatus());
        technicalProject.setStartDate(dto.getStartDate());
        technicalProject.setEndDate(dto.getEndDate());

        return toResponse(technicalProjectRepository.save(technicalProject));
    }

    @Transactional
    public TechnicalProjectResponseDTO update(UUID id, TechnicalProjectRequestDTO dto) {
        TechnicalProject technicalProject = technicalProjectRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projeto Técnico com id:" + id + " não encontrado para atualização"));

        requireOwner(technicalProject);
        if (!technicalProject.getRequester().getId().equals(dto.getRequesterId())) {
            if (dto.getRequesterId() == null) {
                throw new InvalidFieldException("Solicitante é obrigatório e não pode ser removido");
            }
            Requester destination = resolveRequester(dto.getRequesterId());
            rbac.requireOwnCompany(destination.getCompany().getId());
            throw new BusinessRuleException("Solicitante do projeto não pode ser alterado");
        }
        LocalUnit unit = resolveLocalUnit(dto.getLocalUnitId());
        validateUnit(technicalProject.getRequester(), unit);
        technicalProject.setLocalUnit(unit);
        if (dto.getStatus() != null) {
            technicalProject.setStatus(dto.getStatus());
        }
        technicalProject.setStartDate(dto.getStartDate());
        technicalProject.setEndDate(dto.getEndDate());

        return toResponse(technicalProjectRepository.save(technicalProject));
    }

    @Transactional
    public void deleteById(UUID id) {
        TechnicalProject project = technicalProjectRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto Técnico não encontrado com ID: " + id));
        requireOwner(project);

        if (technicalServiceRepository.existsByTechnicalProjectIdAndStatusIn(
                id, List.of(ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS))) {
            throw new ResourceInUseException(
                    "Projeto Técnico não pode ser excluído: possui serviço técnico em aberto ou em andamento");
        }

        technicalProjectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TechnicalProjectResponseDTO findById(UUID id) {
        TechnicalProject technicalProject = technicalProjectRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projeto Técnico não encontrado com ID: " + id));
        requireOwner(technicalProject);
        return toResponse(technicalProject);
    }

    @Transactional(readOnly = true)
    public TechnicalProjectResponseDTO findById(UUID id, UUID companyId) {
        TechnicalProject technicalProject = technicalProjectRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto Técnico não encontrado com ID: " + id));
        requireOwner(technicalProject);
        rbac.requireOwnCompany(companyId);
        return toResponse(technicalProject);
    }

    @Transactional(readOnly = true)
    public List<TechnicalProjectResponseDTO> findByRequester(UUID requesterId) {
        Requester requester = resolveRequester(requesterId);
        rbac.requireOwnCompany(requester.getCompany().getId());
        return technicalProjectRepository.findByRequesterId(requesterId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicalProjectResponseDTO> findAllByCompany(UUID companyId) {
        rbac.requireOwnCompany(companyId);
        return technicalProjectRepository.findByRequester_Company_Id(rbac.getCurrentCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private Requester resolveRequester(UUID requesterId) {
        if (requesterId == null) {
            throw new InvalidFieldException("Solicitante é obrigatório");
        }
        return requesterRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitante não encontrado com ID: " + requesterId));
    }

    private LocalUnit resolveLocalUnit(UUID localUnitId) {
        if (localUnitId == null) {
            return null;
        }
        return localUnitRepository.findForManagementById(localUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + localUnitId));
    }

    private void requireOwner(TechnicalProject project) {
        rbac.requireOwnCompany(project.getRequester() == null ? null : project.getRequester().getCompany().getId());
    }

    private void validateUnit(Requester requester, LocalUnit unit) {
        if (unit != null) {
            rbac.requireOwnCompany(unit.getRequester().getCompany().getId());
            if (!requester.getId().equals(unit.getRequester().getId())) {
                throw new BusinessRuleException("Unidade Local deve pertencer ao solicitante do projeto");
            }
        }
    }

    private TechnicalProjectResponseDTO toResponse(TechnicalProject entity) {
        TechnicalProjectResponseDTO response = new TechnicalProjectResponseDTO();
        response.setId(entity.getId());
        response.setRequesterId(entity.getRequester() == null ? null : entity.getRequester().getId());
        response.setLocalUnitId(entity.getLocalUnit() == null ? null : entity.getLocalUnit().getId());
        response.setStatus(entity.getStatus());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        return response;
    }
}
