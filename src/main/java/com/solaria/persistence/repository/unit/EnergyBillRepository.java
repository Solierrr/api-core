package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.EnergyBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface EnergyBillRepository extends JpaRepository<EnergyBill, UUID> {

    @EntityGraph(attributePaths = {"localUnit.requester.company"})
    @Query("select entity from EnergyBill entity where entity.id = :id")
    Optional<EnergyBill> findForManagementById(UUID id);

    Optional<EnergyBill> findByIdAndLocalUnit_Requester_Company_Id(UUID id, UUID companyId);

    @EntityGraph(attributePaths = {"localUnit.requester.company"})
    List<EnergyBill> findByLocalUnitId(UUID localUnitId);

    boolean existsByLocalUnitId(UUID localUnitId);
}
