package com.solaria.persistence.repository.identity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solaria.persistence.domain.entity.identity.PlatformAdmin;

public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<PlatformAdmin> findByUserId(UUID userId);

}
