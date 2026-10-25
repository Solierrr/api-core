package com.solaria.persistence.repository.shared;

import com.solaria.persistence.domain.entity.shared.Geolocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface GeolocationRepository extends JpaRepository<Geolocation, UUID> {

    @Query("""
            select geolocation
            from Geolocation geolocation
            join fetch geolocation.address address
            where address.id in :addressIds
            order by address.id asc, geolocation.id asc
            """)
    List<Geolocation> findWithAddressByAddressIds(@Param("addressIds") Collection<UUID> addressIds);

    List<Geolocation> findByAddressId(UUID addressId);

    boolean existsByAddressId(UUID addressId);
}
