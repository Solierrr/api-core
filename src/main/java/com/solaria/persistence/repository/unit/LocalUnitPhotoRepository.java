package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.LocalUnitPhoto;
import com.solaria.persistence.domain.enums.unit.LocalUnitPhotoType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface LocalUnitPhotoRepository extends JpaRepository<LocalUnitPhoto, UUID> {

    List<LocalUnitPhoto> findByLocalUnitIdOrderByCreatedAtDesc(UUID localUnitId);

    List<LocalUnitPhoto> findByLocalUnitIdInOrderByCreatedAtDesc(Collection<UUID> localUnitIds);

    Optional<LocalUnitPhoto> findByLocalUnitIdAndType(UUID localUnitId, LocalUnitPhotoType type);

    Optional<LocalUnitPhoto> findByIdAndLocalUnitId(UUID id, UUID localUnitId);

    boolean existsByLocalUnitId(UUID localUnitId);
}
