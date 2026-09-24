package com.solaria.persistence.repository.shared;

import com.solaria.persistence.domain.entity.shared.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
