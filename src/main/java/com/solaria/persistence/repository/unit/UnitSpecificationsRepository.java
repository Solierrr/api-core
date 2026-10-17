package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.UnitSpecifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitSpecificationsRepository extends JpaRepository<UnitSpecifications, UUID> {

    @EntityGraph(attributePaths = {"localUnit.requester.company"})
    @Query("select entity from UnitSpecifications entity where entity.id = :id")
    Optional<UnitSpecifications> findForManagementById(UUID id);

    Optional<UnitSpecifications> findByIdAndLocalUnit_Requester_Company_Id(UUID id, UUID companyId);

    @EntityGraph(attributePaths = {"localUnit.requester.company"})
    List<UnitSpecifications> findByLocalUnitIdOrderByDateDesc(UUID localUnitId);

    boolean existsByLocalUnitId(UUID localUnitId);
}
