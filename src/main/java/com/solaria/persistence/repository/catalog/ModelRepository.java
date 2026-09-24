package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ModelRepository extends JpaRepository<Model, UUID> {

    List<Model> findByStatus(ModelStatus status);
}
