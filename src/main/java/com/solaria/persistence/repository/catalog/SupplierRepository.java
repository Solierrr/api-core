package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.solaria.persistence.domain.enums.catalog.SupplierStatus;
import com.solaria.persistence.domain.enums.company.CompanyStatus;

public interface SupplierRepository extends JpaRepository<Supplier, UUID>, JpaSpecificationExecutor<Supplier> {

    List<Supplier> findByCompanyId(UUID companyId);

    Optional<Supplier> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyId(UUID companyId);

    @EntityGraph(attributePaths = {"company", "company.address"})
    List<Supplier> findByStatusAndCompany_Status(SupplierStatus status, CompanyStatus companyStatus);
}
