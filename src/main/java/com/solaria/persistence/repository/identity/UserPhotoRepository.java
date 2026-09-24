package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.UserPhoto;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserPhotoRepository extends JpaRepository<UserPhoto, UUID> {

    List<UserPhoto> findByUserIdOrderByTypeAsc(UUID userId);

    Optional<UserPhoto> findByUserIdAndType(UUID userId, PhotoType type);

    boolean existsByUserId(UUID userId);
}
