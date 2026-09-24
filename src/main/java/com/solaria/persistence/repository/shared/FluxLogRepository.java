package com.solaria.persistence.repository.shared;

import com.solaria.persistence.domain.entity.shared.FluxLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FluxLogRepository extends JpaRepository<FluxLog, UUID> {

    List<FluxLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
