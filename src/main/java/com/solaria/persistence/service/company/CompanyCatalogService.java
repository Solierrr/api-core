package com.solaria.persistence.service.company;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.enums.catalog.SupplierStatus;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.response.company.CatalogCompanyResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.company.CompanyRepository;

@Service
public class CompanyCatalogService {
    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;

    public CompanyCatalogService(SupplierRepository supplierRepository, CompanyRepository companyRepository) {
        this.supplierRepository = supplierRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogCompanyResponseDTO> findAll() {
        return supplierRepository.findByStatusAndCompany_Status(SupplierStatus.ACTIVE, CompanyStatus.APPROVED)
                .stream()
                .map(supplier -> supplier.getCompany())
                .filter(company -> company.getType() == CompanyType.SUPPLIER)
                .distinct()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogCompanyResponseDTO findBySlug(String slug) {
        Company company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        boolean activeSupplier = supplierRepository.findByCompanyId(company.getId()).stream()
                .anyMatch(supplier -> supplier.getStatus() == SupplierStatus.ACTIVE);
        if (company.getStatus() != CompanyStatus.APPROVED || company.getType() != CompanyType.SUPPLIER || !activeSupplier) {
            throw new ResourceNotFoundException("Empresa não encontrada");
        }
        return toResponse(company);
    }

    private CatalogCompanyResponseDTO toResponse(Company company) {
        return new CatalogCompanyResponseDTO(
                company.getId(),
                company.getTradeName(),
                company.getSlug(),
                company.getAddress() == null ? null : company.getAddress().getCity(),
                company.getAddress() == null ? null : company.getAddress().getState());
    }
}
