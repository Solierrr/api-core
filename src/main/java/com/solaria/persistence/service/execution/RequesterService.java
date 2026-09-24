package com.solaria.persistence.service.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.company.BusinessContact;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.dto.request.execution.RequesterRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;
import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;
import com.solaria.persistence.dto.response.execution.RequesterResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.proposal.ProposalRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;

@Service
public class RequesterService {

    private final RequesterRepository requesterRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final ProposalRepository proposalRepository;
    private final TechnicalProjectRepository technicalProjectRepository;

    public RequesterService(RequesterRepository requesterRepository,
                            CompanyRepository companyRepository,
                            SupplierRepository supplierRepository,
                            ProposalRepository proposalRepository,
                            TechnicalProjectRepository technicalProjectRepository) {
        this.requesterRepository = requesterRepository;
        this.companyRepository = companyRepository;
        this.supplierRepository = supplierRepository;
        this.proposalRepository = proposalRepository;
        this.technicalProjectRepository = technicalProjectRepository;
    }

    @Transactional
    public RequesterResponseDTO save(RequesterRequestDTO dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + dto.getCompanyId()));

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new BusinessRuleException(
                    "Empresa " + company.getId() + " não pode se tornar demandante: status " + company.getStatus());
        }

        if (supplierRepository.existsByCompanyId(dto.getCompanyId())) {
            throw new BusinessRuleException(
                    "Empresa " + dto.getCompanyId() + " já é fornecedora e não pode ser demandante");
        }

        if (requesterRepository.existsByCompanyId(dto.getCompanyId())) {
            throw new DuplicateResourceException(
                    "Demandante já cadastrado para a empresa: " + dto.getCompanyId());
        }

        Requester requester = new Requester();
        requester.setCompany(company);
        requester.setBusinessType(dto.getBusinessType());

        return toResponse(requesterRepository.save(requester));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!requesterRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Demandante com id:" + id + " não encontrado(a) para exclusão");
        }

        if (proposalRepository.existsByRequesterId(id)) {
            throw new ResourceInUseException("Demandante não pode ser excluído(a): possui proposta vinculada(s)");
        }
        if (technicalProjectRepository.existsByRequesterId(id)) {
            throw new ResourceInUseException("Demandante não pode ser excluído(a): possui projeto técnico vinculado(s)");
        }

        requesterRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public RequesterResponseDTO findById(UUID id) {
        Requester requester = requesterRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Demandante não encontrado com ID: " + id));
        return toResponse(requester);
    }

    @Transactional(readOnly = true)
    public RequesterResponseDTO findById(UUID id, UUID companyId) {
        Requester requester = requesterRepository.findByIdAndCompanyId(id, companyId).orElseThrow(
                () -> new ResourceNotFoundException("Demandante não encontrado com ID: " + id));
        return toResponse(requester);
    }

    @Transactional(readOnly = true)
    public List<RequesterResponseDTO> findAll() {
        return requesterRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RequesterResponseDTO> findByCompany(UUID companyId) {
        return requesterRepository.findByCompanyId(companyId).stream().map(this::toResponse).toList();
    }

    private RequesterResponseDTO toResponse(Requester requester) {
        RequesterResponseDTO dto = new RequesterResponseDTO();
        dto.setId(requester.getId());
        dto.setCompany(toCompanyResponse(requester.getCompany()));
        dto.setBusinessType(requester.getBusinessType());
        return dto;
    }

    private CompanyResponseDTO toCompanyResponse(Company company) {
        if (company == null) {
            return null;
        }
        CompanyResponseDTO dto = new CompanyResponseDTO();
        dto.setId(company.getId());
        dto.setStatus(company.getStatus());
        dto.setCnpj(company.getCnpj());
        dto.setTradeName(company.getTradeName());
        dto.setCorporateName(company.getCorporateName());
        dto.setAddress(toAddressResponse(company.getAddress()));
        dto.setBusinessContact(toBusinessContactResponse(company.getBusinessContact()));
        return dto;
    }

    private AddressResponseDTO toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponseDTO.builder()
                .id(address.getId())
                .state(address.getState())
                .city(address.getCity())
                .neighborhood(address.getNeighborhood())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .number(address.getNumber())
                .build();
    }

    private BusinessContactResponseDTO toBusinessContactResponse(BusinessContact businessContact) {
        if (businessContact == null) {
            return null;
        }
        BusinessContactResponseDTO dto = new BusinessContactResponseDTO();
        dto.setId(businessContact.getId());
        dto.setCompanyEmail(businessContact.getCompanyEmail());
        dto.setPhone(businessContact.getPhone());
        dto.setWebsite(businessContact.getWebsite());
        return dto;
    }
}
