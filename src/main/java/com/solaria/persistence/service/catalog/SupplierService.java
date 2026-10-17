package com.solaria.persistence.service.catalog;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.solaria.persistence.mapper.company.CompanyResponseMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.dto.request.catalog.SupplierSearchFilterDTO;
import com.solaria.persistence.dto.request.catalog.SupplierRequestDTO;
import com.solaria.persistence.dto.response.catalog.SupplierResponseDTO;
import com.solaria.persistence.dto.response.catalog.SupplierSearchResponseDTO;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.catalog.SupplierStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.specification.catalog.SupplierSpecification;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;


@Service
public class SupplierService {

    private static final Map<SupplierStatus, Set<SupplierStatus>> ALLOWED_TRANSITIONS = Map.of(
            SupplierStatus.ACTIVE, Set.of(SupplierStatus.SUSPENDED, SupplierStatus.DEACTIVATED),
            SupplierStatus.SUSPENDED, Set.of(SupplierStatus.ACTIVE, SupplierStatus.DEACTIVATED),
            SupplierStatus.DEACTIVATED, Set.of(SupplierStatus.ACTIVE)
    );

    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;
    private final RequesterRepository requesterRepository;
    private final RbacAuthorizationService rbac;
    private final SupplierCacheService supplierCacheService;

    public SupplierService(SupplierRepository supplierRepository,
                           CompanyRepository companyRepository,
                           RequesterRepository requesterRepository,
                           RbacAuthorizationService rbac,
                           SupplierCacheService supplierCacheService) {
        this.supplierRepository = supplierRepository;
        this.companyRepository = companyRepository;
        this.requesterRepository = requesterRepository;
        this.rbac = rbac;
        this.supplierCacheService = supplierCacheService;
    }

    @Transactional
    public SupplierResponseDTO save(SupplierRequestDTO dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + dto.getCompanyId()));

        rbac.requireOwnCompany(company.getId());

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new BusinessRuleException(
                    "Empresa " + company.getId() + " não pode se tornar fornecedora: status " + company.getStatus());
        }

        if (requesterRepository.existsByCompanyId(dto.getCompanyId())) {
            throw new BusinessRuleException(
                    "Empresa " + dto.getCompanyId() + " já é demandante e não pode ser fornecedora");
        }

        if (supplierRepository.existsByCompanyId(dto.getCompanyId())) {
            throw new DuplicateResourceException(
                    "Fornecedor já cadastrado para a empresa: " + dto.getCompanyId());
        }

        Supplier supplier = new Supplier();
        supplier.setCompany(company);
        supplier.setBusinessType(dto.getBusinessType());

        return toResponse(supplierRepository.save(supplier), rbac.findCurrentCompanyId().orElse(null));
    }

    @Transactional
    public SupplierResponseDTO activate(UUID id) {
        return applyTransition(id, SupplierStatus.ACTIVE);
    }

    @Transactional
    public SupplierResponseDTO suspend(UUID id) {
        return applyTransition(id, SupplierStatus.SUSPENDED);
    }

    @Transactional
    public SupplierResponseDTO deactivate(UUID id) {
        return applyTransition(id, SupplierStatus.DEACTIVATED);
    }

    @Transactional(readOnly = true)
    public SupplierResponseDTO findById(UUID id) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Fornecedor não encontrado com ID: " + id));
        return toResponse(supplier, rbac.findCurrentCompanyId().orElse(null));
    }

    @Transactional(readOnly = true)
    public SupplierResponseDTO findById(UUID id, UUID companyId) {
        Supplier supplier = supplierRepository.findByIdAndCompanyId(id, companyId).orElseThrow(
                () -> new ResourceNotFoundException("Fornecedor não encontrado com ID: " + id));
        return toResponse(supplier, rbac.findCurrentCompanyId().orElse(null));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> findAll() {
        UUID actorCompanyId = rbac.findCurrentCompanyId().orElse(null);
        return supplierRepository.findAll().stream()
                .map(item -> toResponse(item, actorCompanyId)).toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> findByCompany(UUID companyId) {
        UUID actorCompanyId = rbac.findCurrentCompanyId().orElse(null);
        return supplierRepository.findByCompanyId(companyId).stream()
                .map(item -> toResponse(item, actorCompanyId)).toList();
    }

    @Transactional(readOnly = true)
    public SupplierSearchResponseDTO search(SupplierSearchFilterDTO filters) {

        SupplierSearchResponseDTO cachedResponse = supplierCacheService.find(filters).orElse(null);

        if (cachedResponse != null) {
            cachedResponse.getContent().forEach(item -> item.getCompany().setBusinessContact(null));
            return cachedResponse;
        }

        Pageable pageable = PageRequest.of(
                filters.getPage(),
                filters.getSize(),
                Sort.by("company.tradeName")
                        .ascending()
                        .and(Sort.by("id").ascending()));

        Page<Supplier> result = supplierRepository.findAll(
                SupplierSpecification.withFilters(filters),
                pageable);

        SupplierSearchResponseDTO response = new SupplierSearchResponseDTO();
        response.setContent(result.getContent().stream().map(item -> toResponse(item, null)).toList());
        response.setPage(result.getNumber());
        response.setSize(result.getSize());
        response.setTotalElements(result.getTotalElements());
        response.setTotalPages(result.getTotalPages());
        response.setFirst(result.isFirst());
        response.setLast(result.isLast());

        supplierCacheService.handleCacheMiss(filters, response);

        return response;
    }

    private SupplierResponseDTO applyTransition(UUID id, SupplierStatus target) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Fornecedor não encontrado com ID: " + id));

        rbac.requireOwnCompany(supplier.getCompany().getId());

        validateTransition(supplier.getStatus(), target);

        supplier.setStatus(target);
        return toResponse(supplierRepository.save(supplier), rbac.findCurrentCompanyId().orElse(null));
    }

    private void validateTransition(SupplierStatus current, SupplierStatus target) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + current + " → " + target);
        }
    }

    private SupplierResponseDTO toResponse(Supplier supplier, UUID actorCompanyId) {
        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setId(supplier.getId());
        dto.setStatus(supplier.getStatus());
        dto.setBusinessType(supplier.getBusinessType());
        dto.setCompany(CompanyResponseMapper.toResponse(supplier.getCompany(), actorCompanyId));
        return dto;
    }

}
