package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.LocalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface LocalUnitRepository extends JpaRepository<LocalUnit, UUID> {

    @EntityGraph(attributePaths = {"requester.company"})
    @Query("select entity from LocalUnit entity where entity.id = :id")
    Optional<LocalUnit> findForManagementById(UUID id);

    @EntityGraph(attributePaths = "address")
    List<LocalUnit> findByRequester_Company_Id(UUID companyId);

    Optional<LocalUnit> findByIdAndRequester_Company_Id(UUID id, UUID companyId);

    @EntityGraph(attributePaths = "address")
    List<LocalUnit> findByRequesterId(UUID requesterId);

    boolean existsByAddressId(UUID addressId);

    boolean existsByRequesterId(UUID requesterId);
}
