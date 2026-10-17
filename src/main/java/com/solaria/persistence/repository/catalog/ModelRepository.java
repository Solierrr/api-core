package com.solaria.persistence.repository.catalog;

import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ModelRepository extends JpaRepository<Model, UUID> {

    List<Model> findByStatus(ModelStatus status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "update model set fk_brand = :target where fk_brand = :source", nativeQuery = true)
    void reassignBrand(UUID source, UUID target);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Model m where m.id = :id")
    java.util.Optional<Model> lockById(@org.springframework.data.repository.query.Param("id") UUID id);
}
