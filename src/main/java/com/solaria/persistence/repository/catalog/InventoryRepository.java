package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findBySupplier_Company_Id(UUID companyId);

    Optional<Inventory> findByIdAndSupplier_Company_Id(UUID id, UUID companyId);

    boolean existsBySupplierIdAndModelId(UUID supplierId, UUID modelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.supplier.id = :supplierId and i.model.id = :modelId")
    Optional<Inventory> findBySupplierIdAndModelIdForUpdate(@Param("supplierId") UUID supplierId,
                                                            @Param("modelId") UUID modelId);

    List<Inventory> findBySupplierId(UUID supplierId);

    boolean existsByModelId(UUID modelId);

}
