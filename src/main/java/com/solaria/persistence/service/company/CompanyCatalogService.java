package com.solaria.persistence.service.company;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.company.CompanyPhoto;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.domain.enums.catalog.SupplierStatus;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.response.company.CatalogCompanyResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.company.CompanyPhotoRepository;
import com.solaria.persistence.repository.company.CompanyRepository;

@Service
public class CompanyCatalogService {
    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;
    private final CompanyPhotoRepository companyPhotoRepository;

    public CompanyCatalogService(SupplierRepository supplierRepository, CompanyRepository companyRepository,
                                 CompanyPhotoRepository companyPhotoRepository) {
        this.supplierRepository = supplierRepository;
        this.companyRepository = companyRepository;
        this.companyPhotoRepository = companyPhotoRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogCompanyResponseDTO> findAll() {
        List<Company> companies = supplierRepository.findByStatusAndCompany_Status(SupplierStatus.ACTIVE, CompanyStatus.APPROVED)
                .stream()
                .map(supplier -> supplier.getCompany())
                .filter(company -> company.getType() == CompanyType.SUPPLIER)
                .distinct()
                .toList();
        Map<UUID, Map<PhotoType, String>> photos = new java.util.HashMap<>();
        for (CompanyPhoto photo : companyPhotoRepository.findByCompanyIdIn(companies.stream().map(Company::getId).toList())) {
            photos.computeIfAbsent(photo.getCompany().getId(), key -> new EnumMap<>(PhotoType.class)).put(photo.getType(), photo.getUrl());
        }
        return companies.stream().map(company -> toResponse(company, photos.getOrDefault(company.getId(), Map.of()))).toList();
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
        Map<PhotoType, String> photos = new EnumMap<>(PhotoType.class);
        companyPhotoRepository.findByCompanyIdIn(List.of(company.getId())).forEach(photo -> photos.put(photo.getType(), photo.getUrl()));
        return toResponse(company, photos);
    }

    private CatalogCompanyResponseDTO toResponse(Company company, Map<PhotoType, String> photos) {
        return new CatalogCompanyResponseDTO(
                company.getId(),
                company.getTradeName(),
                company.getSlug(),
                company.getAddress() == null ? null : company.getAddress().getCity(),
                company.getAddress() == null ? null : company.getAddress().getState(),
                photos.get(PhotoType.PROFILE),
                photos.get(PhotoType.BANNER));
    }
}
