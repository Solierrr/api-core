package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.EnergyBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface EnergyBillRepository extends JpaRepository<EnergyBill, UUID> {

    Optional<EnergyBill> findByIdAndLocalUnit_Requester_Company_Id(UUID id, UUID companyId);

    List<EnergyBill> findByLocalUnitId(UUID localUnitId);

    boolean existsByLocalUnitId(UUID localUnitId);
}
