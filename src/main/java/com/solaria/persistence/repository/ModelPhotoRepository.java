package com.solaria.persistence.repository;

import com.solaria.persistence.domain.entity.ModelPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ModelPhotoRepository extends JpaRepository<ModelPhoto, UUID> {

    List<ModelPhoto> findByModelIdOrderByCreatedAtDesc(UUID modelId);

    Optional<ModelPhoto> findByIdAndModelId(UUID id, UUID modelId);

    boolean existsByModelId(UUID modelId);
}
