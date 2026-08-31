package com.solaria.persistence.service.company;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.company.BusinessContact;
import com.solaria.persistence.dto.request.company.BusinessContactRequestDTO;
import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.BusinessContactRepository;
import com.solaria.persistence.repository.company.CompanyRepository;

@Service
public class BusinessContactService {

    private final BusinessContactRepository businessContactRepository;
    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;

    public BusinessContactService(BusinessContactRepository businessContactRepository,
                                  CompanyRepository companyRepository,
                                  ObjectMapper objectMapper) {
        this.businessContactRepository = businessContactRepository;
        this.companyRepository = companyRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BusinessContactResponseDTO save(BusinessContactRequestDTO dto) {
        BusinessContact businessContact = new BusinessContact();
        businessContact.setCompanyEmail(dto.getCompanyEmail());
        businessContact.setPhone(dto.getPhone());
        businessContact.setWebsite(dto.getWebsite());

        return toResponse(businessContactRepository.save(businessContact));
    }

    @Transactional
    public BusinessContactResponseDTO update(UUID id, BusinessContactRequestDTO dto) {
        BusinessContact businessContact = businessContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contato empresarial com id:" + id + " não encontrado(a) para atualização"));

        businessContact.setCompanyEmail(dto.getCompanyEmail());
        businessContact.setPhone(dto.getPhone());
        businessContact.setWebsite(dto.getWebsite());

        return toResponse(businessContactRepository.save(businessContact));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!businessContactRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Contato empresarial com id:" + id + " não encontrado(a) para exclusão");
        }
        if (companyRepository.existsByBusinessContactId(id)) {
            throw new ResourceInUseException(
                    "Contato empresarial não pode ser excluído(a): possui empresa vinculada");
        }
        businessContactRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BusinessContactResponseDTO findById(UUID id) {
        BusinessContact businessContact = businessContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contato empresarial não encontrado(a) com ID: " + id));
        return toResponse(businessContact);
    }

    @Transactional(readOnly = true)
    public List<BusinessContactResponseDTO> findAll() {
        return businessContactRepository.findAll().stream().map(this::toResponse).toList();
    }

    private BusinessContactResponseDTO toResponse(BusinessContact businessContact) {
        return objectMapper.convertValue(businessContact, BusinessContactResponseDTO.class);
    }
}
