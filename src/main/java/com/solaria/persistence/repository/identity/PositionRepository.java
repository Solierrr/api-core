package com.solaria.persistence.repository.identity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solaria.persistence.domain.entity.identity.Position;


public interface PositionRepository extends JpaRepository<Position, UUID> {

    Optional<Position> findByName(String name);
}
