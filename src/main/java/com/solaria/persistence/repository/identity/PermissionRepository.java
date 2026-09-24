package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    boolean existsByPermissionName(String permissionName);

    boolean existsByPermissionNameAndIdNot(String permissionName, UUID id);
}
