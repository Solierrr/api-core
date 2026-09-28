package com.solaria.persistence.repository.shared;

import com.solaria.persistence.domain.entity.shared.Geolocalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface GeolocalizationRepository extends JpaRepository<Geolocalization, UUID> {

    @Query("""
            select geolocalization
            from Geolocalization geolocalization
            join fetch geolocalization.address address
            where address.id in :addressIds
            order by address.id asc, geolocalization.id asc
            """)
    List<Geolocalization> findWithAddressByAddressIds(@Param("addressIds") Collection<UUID> addressIds);

    List<Geolocalization> findByAddressId(UUID addressId);

    boolean existsByAddressId(UUID addressId);
}
