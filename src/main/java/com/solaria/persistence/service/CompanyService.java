package com.solaria.persistence.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.Address;
import com.solaria.persistence.domain.entity.BusinessContact;
import com.solaria.persistence.domain.entity.Company;
import com.solaria.persistence.domain.enums.CompanyStatus;
import com.solaria.persistence.dto.request.CompanyRequestDTO;
import com.solaria.persistence.dto.response.AddressResponseDTO;
import com.solaria.persistence.dto.response.BusinessContactResponseDTO;
import com.solaria.persistence.dto.response.CompanyResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.AddressRepository;
import com.solaria.persistence.repository.BusinessContactRepository;
import com.solaria.persistence.repository.CompanyPhotoRepository;
import com.solaria.persistence.repository.CompanyPositionsRepository;
import com.solaria.persistence.repository.CompanyRepository;
import com.solaria.persistence.repository.RequesterRepository;
import com.solaria.persistence.repository.SupplierRepository;
import com.solaria.persistence.repository.TechnicianAffiliationRepository;
import com.solaria.persistence.repository.UserCompanyRepository;
import com.solaria.persistence.util.SlugUtil;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final BusinessContactRepository businessContactRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final SupplierRepository supplierRepository;
    private final RequesterRepository requesterRepository;
    private final TechnicianAffiliationRepository technicianAffiliationRepository;
    private final CompanyPositionsRepository companyPositionsRepository;
    private final CompanyPhotoRepository companyPhotoRepository;

    public CompanyService(CompanyRepository companyRepository,
                          AddressRepository addressRepository,
                          BusinessContactRepository businessContactRepository,
                          UserCompanyRepository userCompanyRepository,
                          SupplierRepository supplierRepository,
                          RequesterRepository requesterRepository,
                          TechnicianAffiliationRepository technicianAffiliationRepository,
                          CompanyPositionsRepository companyPositionsRepository,
                          CompanyPhotoRepository companyPhotoRepository) {
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.businessContactRepository = businessContactRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.supplierRepository = supplierRepository;
        this.requesterRepository = requesterRepository;
        this.technicianAffiliationRepository = technicianAffiliationRepository;
        this.companyPositionsRepository = companyPositionsRepository;
        this.companyPhotoRepository = companyPhotoRepository;
    }

    @Transactional
    public CompanyResponseDTO save(CompanyRequestDTO dto) {
        String cnpj = dto.getCnpj();

        Address address = resolveAddress(dto.getAddressId());
        BusinessContact businessContact = resolveBusinessContact(dto.getBusinessContactId());

        if (companyRepository.existsByCnpj(cnpj)) {
            throw new DuplicateResourceException(
                    "Empresa já cadastrada para o CNPJ: " + cnpj);
        }

        Company company = new Company();
        company.setAddress(address);
        company.setBusinessContact(businessContact);
        company.setCnpj(cnpj);
        company.setTradeName(dto.getTradeName());
        company.setCorporateName(dto.getCorporateName());
        company.setSlug(generateUniqueSlug(dto.getTradeName()));

        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponseDTO update(UUID id, CompanyRequestDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa com id:" + id + " não encontrada para atualização"));

        if (!company.getCnpj().equals(dto.getCnpj())) {
            throw new InvalidFieldException("CNPJ imutável: " + dto.getCnpj());
        }

        company.setAddress(resolveAddress(dto.getAddressId()));
        company.setBusinessContact(resolveBusinessContact(dto.getBusinessContactId()));
        company.setTradeName(dto.getTradeName());
        company.setCorporateName(dto.getCorporateName());

        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponseDTO approve(UUID id) {
        return applyStatus(id, CompanyStatus.APPROVED);
    }

    @Transactional
    public CompanyResponseDTO reject(UUID id) {
        return applyStatus(id, CompanyStatus.REJECTED);
    }


    private CompanyResponseDTO applyStatus(UUID id, CompanyStatus target) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + id));
        if (company.getStatus() != CompanyStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + company.getStatus() + " → " + target);
        }
        company.setStatus(target);
        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponseDTO attachAddress(UUID companyId, UUID addressId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + companyId));

        company.setAddress(addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado com ID: " + addressId)));

        return toResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponseDTO attachBusinessContact(UUID companyId, UUID businessContactId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + companyId));

        company.setBusinessContact(businessContactRepository.findById(businessContactId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contato Empresarial não encontrado com ID: " + businessContactId)));

        return toResponse(companyRepository.save(company));
    }


    @Transactional
    public void deleteById(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Empresa com id:" + id + " não encontrada para exclusão");
        }

        if (userCompanyRepository.existsByCompanyId(id)
                || supplierRepository.existsByCompanyId(id)
                || requesterRepository.existsByCompanyId(id)
                || technicianAffiliationRepository.existsByCompanyId(id)
                || companyPositionsRepository.existsByCompanyId(id)
                || companyPhotoRepository.existsByCompanyId(id)) {
            throw new ResourceInUseException(
                    "Empresa não pode ser excluída: possui vínculo(s) associado(s)");
        }

        companyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + id));
        return toResponse(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> findAll() {
        return companyRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findByCnpj(String cnpj) {
        String normalized = normalizeCnpj(cnpj);
        Company company = companyRepository.findByCnpj(normalized)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com CNPJ: " + normalized));
        return toResponse(company);
    }


    private String normalizeCnpj(String cnpj) {
        return cnpj == null ? null : cnpj.replaceAll("\\D", "");
    }

    private Address resolveAddress(UUID addressId) {
        if (addressId == null) {
            return null;
        }
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado com ID: " + addressId));
    }

    private BusinessContact resolveBusinessContact(UUID businessContactId) {
        if (businessContactId == null) {
            return null;
        }
        return businessContactRepository.findById(businessContactId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contato Empresarial não encontrado com ID: " + businessContactId));
    }

    private String generateUniqueSlug(String tradeName) {
        String base = SlugUtil.slugify(tradeName);
        String candidate = base;
        int suffix = 2;
        while (companyRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private CompanyResponseDTO toResponse(Company company) {
        CompanyResponseDTO dto = new CompanyResponseDTO();
        dto.setId(company.getId());
        dto.setStatus(company.getStatus());
        dto.setCnpj(company.getCnpj());
        dto.setTradeName(company.getTradeName());
        dto.setCorporateName(company.getCorporateName());
        dto.setSlug(company.getSlug());
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
