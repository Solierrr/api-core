package com.solaria.persistence.service.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.execution.ServiceExecutor;
import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.entity.professional.TechnicianAffiliation;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.dto.request.execution.ServiceExecutorRequestDTO;
import com.solaria.persistence.dto.response.execution.ServiceExecutorResponseDTO;
import com.solaria.persistence.dto.response.professional.TechnicianAffiliationResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.execution.ServiceExecutorRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.repository.professional.TechnicianAffiliationRepository;


@Service
public class ServiceExecutorService {

    private final ServiceExecutorRepository serviceExecutorRepository;
    private final TechnicalServiceRepository technicalServiceRepository;
    private final TechnicianAffiliationRepository technicianAffiliationRepository;
    private final ObjectMapper objectMapper;

    public ServiceExecutorService(ServiceExecutorRepository serviceExecutorRepository,
                                  TechnicalServiceRepository technicalServiceRepository,
                                  TechnicianAffiliationRepository technicianAffiliationRepository,
                                  ObjectMapper objectMapper) {
        this.serviceExecutorRepository = serviceExecutorRepository;
        this.technicalServiceRepository = technicalServiceRepository;
        this.technicianAffiliationRepository = technicianAffiliationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ServiceExecutorResponseDTO save(ServiceExecutorRequestDTO dto) {
        TechnicalService service = technicalServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico não encontrado com ID: " + dto.getServiceId()));

        TechnicianAffiliation technicianAffiliation = technicianAffiliationRepository
                .findById(dto.getTechnicianAffiliationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Afiliação do Técnico não encontrada com ID: " + dto.getTechnicianAffiliationId()));

        validateServiceMutableState(service);

        if (serviceExecutorRepository.existsByServiceIdAndTechnicianAffiliationId(
                dto.getServiceId(), dto.getTechnicianAffiliationId())) {
            throw new DuplicateResourceException(
                    "Afiliação já vinculada como executor deste serviço: " + dto.getTechnicianAffiliationId());
        }

        ServiceExecutor serviceExecutor = new ServiceExecutor();
        serviceExecutor.setService(service);
        serviceExecutor.setTechnicianAffiliation(technicianAffiliation);
        serviceExecutor.setFunction(dto.getFunction());

        return toResponse(serviceExecutorRepository.save(serviceExecutor));
    }

    @Transactional
    public ServiceExecutorResponseDTO updateFunction(UUID id, String function) {
        ServiceExecutor serviceExecutor = serviceExecutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Executor do Serviço com id:" + id + " não encontrado para atualização"));

        if (function == null || function.isBlank()) {
            throw new InvalidFieldException("Função do colaborador é obrigatória");
        }

        validateServiceMutableState(serviceExecutor.getService());

        serviceExecutor.setFunction(function);

        return toResponse(serviceExecutorRepository.save(serviceExecutor));
    }

    @Transactional
    public void deleteById(UUID id) {
        ServiceExecutor serviceExecutor = serviceExecutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Executor do Serviço com id:" + id + " não encontrado para exclusão"));

        validateServiceMutableState(serviceExecutor.getService());

        serviceExecutorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ServiceExecutorResponseDTO> findByService(UUID serviceId) {
        return serviceExecutorRepository.findByServiceId(serviceId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateServiceMutableState(TechnicalService service) {
        ServiceStatus status = service.getStatus();
        if (status != ServiceStatus.OPEN && status != ServiceStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Serviço Técnico não pode ser alterado no status: " + status);
        }
    }

    private ServiceExecutorResponseDTO toResponse(ServiceExecutor entity) {
        ServiceExecutorResponseDTO response = objectMapper.convertValue(entity, ServiceExecutorResponseDTO.class);
        response.setServiceId(entity.getService().getId());
        response.setTechnicianAffiliation(toTechnicianAffiliationResponse(entity.getTechnicianAffiliation()));
        response.setPosition(entity.getFunction());
        return response;
    }

    private TechnicianAffiliationResponseDTO toTechnicianAffiliationResponse(TechnicianAffiliation entity) {
        TechnicianAffiliationResponseDTO response = objectMapper.convertValue(entity, TechnicianAffiliationResponseDTO.class);
        response.setCompanyId(entity.getCompany().getId());
        response.setTechnicianId(entity.getTechnician().getId());
        return response;
    }
}
