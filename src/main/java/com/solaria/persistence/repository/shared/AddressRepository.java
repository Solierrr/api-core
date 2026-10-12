package com.solaria.persistence.repository.shared;

import com.solaria.persistence.domain.entity.shared.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByOwnerCompany_Id(UUID companyId);
}
