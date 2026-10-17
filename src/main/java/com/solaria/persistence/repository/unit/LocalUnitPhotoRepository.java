package com.solaria.persistence.repository.unit;

import com.solaria.persistence.domain.entity.unit.LocalUnitPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface LocalUnitPhotoRepository extends JpaRepository<LocalUnitPhoto, UUID> {

    @EntityGraph(attributePaths = {"localUnit.requester.company"})
    @Query("select entity from LocalUnitPhoto entity where entity.id = :id")
    Optional<LocalUnitPhoto> findForManagementById(UUID id);

    @EntityGraph(attributePaths = {"localUnit.requester.company"})
    List<LocalUnitPhoto> findByLocalUnitIdOrderByCreatedAtDesc(UUID localUnitId);

    Optional<LocalUnitPhoto> findByIdAndLocalUnitId(UUID id, UUID localUnitId);

    boolean existsByLocalUnitId(UUID localUnitId);
}
