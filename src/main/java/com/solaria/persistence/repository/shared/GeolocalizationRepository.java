package com.solaria.persistence.repository.shared;

import com.solaria.persistence.domain.entity.shared.Geolocalization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface GeolocalizationRepository extends JpaRepository<Geolocalization, UUID> {

    List<Geolocalization> findByAddressId(UUID addressId);

    boolean existsByAddressId(UUID addressId);
}
