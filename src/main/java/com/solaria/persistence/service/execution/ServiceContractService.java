package com.solaria.persistence.service.execution;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import com.solaria.persistence.dto.request.execution.ServiceContractRequestDTO;
import com.solaria.persistence.dto.response.execution.ServiceContractResponseDTO;
import com.solaria.persistence.domain.entity.execution.ServiceContract;
import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.execution.ServiceContractRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;


@Service
public class ServiceContractService {

    private final ServiceContractRepository serviceContractRepository;
    private final TechnicalServiceRepository technicalServiceRepository;
    private final RbacAuthorizationService rbac;
    private final ObjectMapper objectMapper;

    public ServiceContractService(ServiceContractRepository serviceContractRepository,
                                  TechnicalServiceRepository technicalServiceRepository,
                                  RbacAuthorizationService rbac,
                                  ObjectMapper objectMapper) {
        this.serviceContractRepository = serviceContractRepository;
        this.technicalServiceRepository = technicalServiceRepository;
        this.rbac = rbac;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ServiceContractResponseDTO save(ServiceContractRequestDTO dto) {
        validateDeliveryDeadline(dto.getDeliveryDeadline());

        TechnicalService service = technicalServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço Técnico não encontrado com ID: " + dto.getServiceId()));

        if (serviceContractRepository.existsByServiceId(dto.getServiceId())) {
            throw new DuplicateResourceException(
                    "Contrato de Serviço já cadastrado para o serviço: " + dto.getServiceId());
        }

        if (service.getStatus() != ServiceStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Contrato de Serviço não pode ser criado no status: " + service.getStatus());
        }

        ServiceContract serviceContract = new ServiceContract();
        serviceContract.setService(service);
        serviceContract.setWarranty(dto.getWarranty());
        serviceContract.setDeliveryDeadline(dto.getDeliveryDeadline());
        serviceContract.setInsurance(dto.getInsurance());
        serviceContract.setUtilityApproval(false);

        return toResponse(serviceContractRepository.save(serviceContract));
    }

    @Transactional
    public ServiceContractResponseDTO update(UUID id, ServiceContractRequestDTO dto) {
        ServiceContract serviceContract = serviceContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contrato de Serviço com id:" + id + " não encontrado para atualização"));

        if (!dto.getServiceId().equals(serviceContract.getService().getId())) {
            throw new InvalidFieldException("serviceId imutável: " + dto.getServiceId());
        }

        validateDeliveryDeadline(dto.getDeliveryDeadline());

        serviceContract.setWarranty(dto.getWarranty());
        serviceContract.setDeliveryDeadline(dto.getDeliveryDeadline());
        serviceContract.setInsurance(dto.getInsurance());

        return toResponse(serviceContractRepository.save(serviceContract));
    }

    @Transactional
    public ServiceContractResponseDTO markUtilityApproved(UUID id) {
        ServiceContract serviceContract = serviceContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contrato de Serviço com id:" + id + " não encontrado para atualização"));

        rbac.requireOwnCompany(serviceContract.getService().getTechnicalProject().getRequester().getCompany().getId());

        serviceContract.setUtilityApproval(true);

        return toResponse(serviceContractRepository.save(serviceContract));
    }

    @Transactional(readOnly = true)
    public ServiceContractResponseDTO findById(UUID id) {
        ServiceContract serviceContract = serviceContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contrato de Serviço não encontrado com ID: " + id));
        return toResponse(serviceContract);
    }

    @Transactional(readOnly = true)
    public ServiceContractResponseDTO findById(UUID id, UUID companyId) {
        ServiceContract serviceContract = serviceContractRepository
                .findByIdAndService_TechnicalProject_Requester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contrato de Serviço não encontrado com ID: " + id));
        return toResponse(serviceContract);
    }

    @Transactional(readOnly = true)
    public ServiceContractResponseDTO findByService(UUID serviceId) {
        ServiceContract serviceContract = serviceContractRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contrato de Serviço não encontrado para o serviço: " + serviceId));
        return toResponse(serviceContract);
    }

    private void validateDeliveryDeadline(LocalDate deliveryDeadline) {
        if (deliveryDeadline != null && deliveryDeadline.isBefore(LocalDate.now())) {
            throw new InvalidFieldException("Prazo de entrega inválido: " + deliveryDeadline);
        }
    }

    private ServiceContractResponseDTO toResponse(ServiceContract entity) {
        ServiceContractResponseDTO response = objectMapper.convertValue(entity, ServiceContractResponseDTO.class);
        response.setServiceId(entity.getService().getId());
        return response;
    }
}
