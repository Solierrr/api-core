package com.solaria.persistence.service.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.response.analytics.AnalyticsDashboardResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsReader reader;
    @Mock
    private RbacAuthorizationService rbac;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private RequesterRepository requesterRepository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(reader, rbac, companyRepository, supplierRepository, requesterRepository);
    }

    @Test
    void platformReadsTheAdminScopeOnly() {
        List<String> tables = new java.util.ArrayList<>(AnalyticsService.PLATFORM_TABLES);
        tables.addAll(AnalyticsService.PLATFORM_DETAIL_TABLES);
        UUID supplierId = UUID.randomUUID();
        Company company = new Company();
        company.setTradeName("Solar XPTO");
        Supplier supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setCompany(company);
        when(reader.read("ADMIN", "*", tables)).thenReturn(Map.of("kpi_supplier_offers_summary",
                List.of(Map.of("supplier_id", supplierId.toString()))));
        when(supplierRepository.findByIdIn(java.util.Set.of(supplierId))).thenReturn(List.of(supplier));
        when(reader.loadedAt()).thenReturn("2026-11-02T10:00:00Z");

        AnalyticsDashboardResponseDTO response = service.platform();

        assertEquals("PLATFORM", response.scope());
        assertEquals("2026-11-02T10:00:00Z", response.loadedAt());
        assertEquals(Map.of(supplierId.toString(), "Solar XPTO"), response.names());
    }

    @Test
    void supplierCompanyReadsItsOwnSupplierId() {
        UUID companyId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        Company company = new Company();
        company.setType(CompanyType.SUPPLIER);
        Supplier supplier = new Supplier();
        supplier.setId(supplierId);
        when(rbac.isPlatformAdmin()).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(supplierRepository.findByCompanyId(companyId)).thenReturn(List.of(supplier));
        when(reader.read("SUPPLIER", supplierId.toString(), AnalyticsService.SUPPLIER_TABLES)).thenReturn(Map.of());

        AnalyticsDashboardResponseDTO response = service.company(companyId);

        assertEquals("SUPPLIER", response.scope());
        assertEquals(companyId, response.companyId());
        verify(rbac).requireOwnCompany(companyId);
    }

    @Test
    void requesterCompanyReadsItsOwnRequesterId() {
        UUID companyId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Company company = new Company();
        company.setType(CompanyType.REQUESTER);
        Requester requester = new Requester();
        requester.setId(requesterId);
        when(rbac.isPlatformAdmin()).thenReturn(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(requesterRepository.findByCompanyId(companyId)).thenReturn(List.of(requester));
        when(reader.read("REQUESTER", requesterId.toString(), AnalyticsService.REQUESTER_TABLES)).thenReturn(Map.of());

        assertEquals("REQUESTER", service.company(companyId).scope());
    }

    @Test
    void platformAdminSkipsTheOwnCompanyCheck() {
        UUID companyId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        Company company = new Company();
        company.setType(CompanyType.SUPPLIER);
        Supplier supplier = new Supplier();
        supplier.setId(supplierId);
        when(rbac.isPlatformAdmin()).thenReturn(true);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(supplierRepository.findByCompanyId(companyId)).thenReturn(List.of(supplier));
        when(reader.read("SUPPLIER", supplierId.toString(), AnalyticsService.SUPPLIER_TABLES)).thenReturn(Map.of());

        service.company(companyId);

        verify(rbac, never()).requireOwnCompany(any());
    }

    @Test
    void anotherCompanyIsRefusedBeforeAnyRead() {
        UUID companyId = UUID.randomUUID();
        when(rbac.isPlatformAdmin()).thenReturn(false);
        org.mockito.Mockito.doThrow(new UnauthorizedAccessException("x")).when(rbac).requireOwnCompany(companyId);

        assertThrows(UnauthorizedAccessException.class, () -> service.company(companyId));
        verify(reader, never()).read(any(), any(), any());
    }

    @Test
    void unknownCompanyIsNotFound() {
        UUID companyId = UUID.randomUUID();
        when(rbac.isPlatformAdmin()).thenReturn(true);
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.company(companyId));
    }
}
