package com.solaria.persistence.repository.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solaria.persistence.domain.entity.identity.PositionPermission;


public interface PositionPermissionRepository extends JpaRepository<PositionPermission, UUID> {

    boolean existsByPositionIdAndPermissionId(UUID positionId, UUID permissionId);

    boolean existsByPositionIdAndPermission_PermissionName(UUID positionId, String permissionName);

    List<PositionPermission> findByPositionId(UUID positionId);

    void deleteByPositionId(UUID positionId);

    void deleteByPermissionId(UUID permissionId);

}
