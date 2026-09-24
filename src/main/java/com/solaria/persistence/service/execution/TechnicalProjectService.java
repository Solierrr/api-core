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

    public TechnicalProjectService(TechnicalProjectRepository technicalProjectRepository,
                                   RequesterRepository requesterRepository,
                                   LocalUnitRepository localUnitRepository,
                                   TechnicalServiceRepository technicalServiceRepository) {
        this.technicalProjectRepository = technicalProjectRepository;
        this.requesterRepository = requesterRepository;
        this.localUnitRepository = localUnitRepository;
        this.technicalServiceRepository = technicalServiceRepository;
    }

    @Transactional
    public TechnicalProjectResponseDTO save(TechnicalProjectRequestDTO dto) {
        TechnicalProject technicalProject = new TechnicalProject();
        technicalProject.setRequester(resolveRequester(dto.getRequesterId()));
        technicalProject.setLocalUnit(resolveLocalUnit(dto.getLocalUnitId()));
        technicalProject.setStatus(dto.getStatus());
        technicalProject.setStartDate(dto.getStartDate());
        technicalProject.setEndDate(dto.getEndDate());

        return toResponse(technicalProjectRepository.save(technicalProject));
    }

    @Transactional
    public TechnicalProjectResponseDTO update(UUID id, TechnicalProjectRequestDTO dto) {
        TechnicalProject technicalProject = technicalProjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projeto Técnico com id:" + id + " não encontrado para atualização"));

        technicalProject.setRequester(resolveRequester(dto.getRequesterId()));
        technicalProject.setLocalUnit(resolveLocalUnit(dto.getLocalUnitId()));
        technicalProject.setStatus(dto.getStatus());
        technicalProject.setStartDate(dto.getStartDate());
        technicalProject.setEndDate(dto.getEndDate());

        return toResponse(technicalProjectRepository.save(technicalProject));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!technicalProjectRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Projeto Técnico com id:" + id + " não encontrado para exclusão");
        }

        if (technicalServiceRepository.existsByTechnicalProjectIdAndStatusIn(
                id, List.of(ServiceStatus.OPEN, ServiceStatus.IN_PROGRESS))) {
            throw new ResourceInUseException(
                    "Projeto Técnico não pode ser excluído: possui serviço técnico em aberto ou em andamento");
        }

        technicalProjectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TechnicalProjectResponseDTO findById(UUID id) {
        TechnicalProject technicalProject = technicalProjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projeto Técnico não encontrado com ID: " + id));
        return toResponse(technicalProject);
    }

    @Transactional(readOnly = true)
    public TechnicalProjectResponseDTO findById(UUID id, UUID companyId) {
        TechnicalProject technicalProject = technicalProjectRepository
                .findByIdAndRequester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projeto Técnico não encontrado com ID: " + id));
        return toResponse(technicalProject);
    }

    @Transactional(readOnly = true)
    public List<TechnicalProjectResponseDTO> findByRequester(UUID requesterId) {
        return technicalProjectRepository.findByRequesterId(requesterId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicalProjectResponseDTO> findAllByCompany(UUID companyId) {
        return technicalProjectRepository.findByRequester_Company_Id(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Requester resolveRequester(UUID requesterId) {
        if (requesterId == null) {
            return null;
        }
        return requesterRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitante não encontrado com ID: " + requesterId));
    }

    private LocalUnit resolveLocalUnit(UUID localUnitId) {
        if (localUnitId == null) {
            return null;
        }
        return localUnitRepository.findById(localUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + localUnitId));
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
