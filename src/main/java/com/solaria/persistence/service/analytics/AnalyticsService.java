package com.solaria.persistence.service.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.response.analytics.AnalyticsDashboardResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

/**
 * Indicadores do dashboard. O isolamento acontece no banco (RLS por contexto de requisição); aqui só se decide
 * qual escopo e qual entidade a requisição pode enxergar.
 */
@Service
public class AnalyticsService {

    static final List<String> PLATFORM_TABLES = List.of(
            "kpi_admin_registrations",
            "kpi_admin_models_by_status",
            "kpi_admin_proposals_by_status");

    /** Tabelas por fornecedor e demandante que o admin da plataforma agrega e ranqueia. */
    static final List<String> PLATFORM_DETAIL_TABLES = List.of(
            "kpi_supplier_offers_summary",
            "kpi_supplier_stock_summary",
            "kpi_supplier_proposals_summary",
            "kpi_requester_summary");

    static final List<String> SUPPLIER_TABLES = List.of(
            "kpi_supplier_offers_summary",
            "kpi_supplier_offers_by_status",
            "kpi_supplier_offer_details",
            "kpi_supplier_stock_summary",
            "kpi_supplier_stock_by_model",
            "kpi_supplier_proposals_summary",
            "kpi_supplier_proposals_by_status",
            "kpi_supplier_accepted_value_by_model",
            "kpi_supplier_accepted_value_by_month",
            "kpi_supplier_accepted_offers_by_date");

    static final List<String> REQUESTER_TABLES = List.of(
            "kpi_requester_summary",
            "kpi_requester_proposals_by_status",
            "kpi_requester_projects_by_status");

    private final AnalyticsReader reader;
    private final RbacAuthorizationService rbac;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final RequesterRepository requesterRepository;

    public AnalyticsService(AnalyticsReader reader, RbacAuthorizationService rbac, CompanyRepository companyRepository,
            SupplierRepository supplierRepository, RequesterRepository requesterRepository) {
        this.reader = reader;
        this.rbac = rbac;
        this.companyRepository = companyRepository;
        this.supplierRepository = supplierRepository;
        this.requesterRepository = requesterRepository;
    }

    public AnalyticsDashboardResponseDTO platform() {
        List<String> tables = new ArrayList<>(PLATFORM_TABLES);
        tables.addAll(PLATFORM_DETAIL_TABLES);
        Map<String, List<Map<String, Object>>> kpis = reader.read("ADMIN", "*", tables);
        return new AnalyticsDashboardResponseDTO("PLATFORM", null, reader.loadedAt(), kpis, names(kpis));
    }

    public AnalyticsDashboardResponseDTO company(UUID companyId) {
        if (!rbac.isPlatformAdmin()) {
            rbac.requireOwnCompany(companyId);
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        if (company.getType() == CompanyType.SUPPLIER) {
            UUID supplierId = supplierRepository.findByCompanyId(companyId).stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado"))
                    .getId();
            return new AnalyticsDashboardResponseDTO("SUPPLIER", companyId, reader.loadedAt(),
                    reader.read("SUPPLIER", supplierId.toString(), SUPPLIER_TABLES), Map.of());
        }
        if (company.getType() == CompanyType.REQUESTER) {
            UUID requesterId = requesterRepository.findByCompanyId(companyId).stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Demandante não encontrado"))
                    .getId();
            return new AnalyticsDashboardResponseDTO("REQUESTER", companyId, reader.loadedAt(),
                    reader.read("REQUESTER", requesterId.toString(), REQUESTER_TABLES), Map.of());
        }
        throw new ResourceNotFoundException("A empresa não tem indicadores");
    }

    private Map<String, String> names(Map<String, List<Map<String, Object>>> kpis) {
        Map<String, String> names = new HashMap<>();
        Set<UUID> supplierIds = ids(kpis.get("kpi_supplier_offers_summary"), "supplier_id");
        supplierRepository.findByIdIn(supplierIds).forEach(
                supplier -> names.put(supplier.getId().toString(), supplier.getCompany().getTradeName()));
        return names;
    }

    private static Set<UUID> ids(List<Map<String, Object>> rows, String column) {
        Set<UUID> ids = new HashSet<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                try {
                    ids.add(UUID.fromString(String.valueOf(row.get(column))));
                } catch (IllegalArgumentException ignored) {
                    // linha sem id válido: sem nome para ela
                }
            }
        }
        return ids;
    }
}
