package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.LocalUnitPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface LocalUnitPhotoRepository extends JpaRepository<LocalUnitPhoto, UUID> {

    List<LocalUnitPhoto> findByLocalUnitIdOrderByCreatedAtDesc(UUID localUnitId);

    Optional<LocalUnitPhoto> findByIdAndLocalUnitId(UUID id, UUID localUnitId);

    boolean existsByLocalUnitId(UUID localUnitId);
}
