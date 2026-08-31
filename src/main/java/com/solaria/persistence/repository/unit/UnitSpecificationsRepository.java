package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.UnitSpecifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitSpecificationsRepository extends JpaRepository<UnitSpecifications, UUID> {

    Optional<UnitSpecifications> findByIdAndLocalUnit_Requester_Company_Id(UUID id, UUID companyId);

    List<UnitSpecifications> findByLocalUnitIdOrderByDateDesc(UUID localUnitId);

    boolean existsByLocalUnitId(UUID localUnitId);
}
