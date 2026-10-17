package com.solaria.persistence.service.company;

import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.company.BusinessContact;
import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyRepository;

@Service
public class BusinessContactService {

    private final RbacAuthorizationService rbac;
    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;

    public BusinessContactService(RbacAuthorizationService rbac,
                                  CompanyRepository companyRepository,
                                  ObjectMapper objectMapper) {
        this.rbac = rbac;
        this.companyRepository = companyRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public BusinessContactResponseDTO findById(UUID id) {
        BusinessContact contact = currentContact();
        if (contact == null || !contact.getId().equals(id)) {
            throw new ResourceNotFoundException("Contato empresarial não encontrado com ID: " + id);
        }
        return toResponse(contact);
    }

    @Transactional(readOnly = true)
    public List<BusinessContactResponseDTO> findAll() {
        BusinessContact contact = currentContact();
        return contact == null ? List.of() : List.of(toResponse(contact));
    }

    private BusinessContact currentContact() {
        return companyRepository.findById(rbac.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"))
                .getBusinessContact();
    }

    private BusinessContactResponseDTO toResponse(BusinessContact businessContact) {
        return objectMapper.convertValue(businessContact, BusinessContactResponseDTO.class);
    }
}
