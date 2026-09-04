package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.LocalUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface LocalUnitRepository extends JpaRepository<LocalUnit, UUID> {

    List<LocalUnit> findByRequester_Company_Id(UUID companyId);

    Optional<LocalUnit> findByIdAndRequester_Company_Id(UUID id, UUID companyId);

    List<LocalUnit> findByRequesterId(UUID requesterId);

    boolean existsByAddressId(UUID addressId);

    boolean existsByRequesterId(UUID requesterId);
}
