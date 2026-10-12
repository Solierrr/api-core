package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.UserPhoto;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserPhotoRepository extends JpaRepository<UserPhoto, UUID> {

    @Query("""
            select photo
            from UserPhoto photo
            join fetch photo.user user
            where user.id in :userIds and photo.type = :type
            order by user.id asc, photo.createdAt desc, photo.id asc
            """)
    List<UserPhoto> findPhotosByUserIdsAndType(
            @Param("userIds") Collection<UUID> userIds,
            @Param("type") PhotoType type);

    List<UserPhoto> findByUserIdOrderByTypeAsc(UUID userId);

    Optional<UserPhoto> findByUserIdAndType(UUID userId, PhotoType type);

    boolean existsByUserId(UUID userId);
}
