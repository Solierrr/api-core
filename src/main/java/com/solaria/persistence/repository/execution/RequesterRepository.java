package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.Requester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequesterRepository extends JpaRepository<Requester, UUID> {

    @EntityGraph(attributePaths = "company")
    List<Requester> findByCompanyId(UUID companyId);

    @EntityGraph(attributePaths = "company")
    Optional<Requester> findOneByCompanyId(UUID companyId);

    @EntityGraph(attributePaths = "company")
    Optional<Requester> findByIdAndCompanyId(UUID id, UUID companyId);

    @Override
    @EntityGraph(attributePaths = "company")
    Optional<Requester> findById(UUID id);

    boolean existsByCompanyId(UUID companyId);
}
