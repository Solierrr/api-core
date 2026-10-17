package com.solaria.persistence.repository.company;

import com.solaria.persistence.domain.entity.company.Company;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CompanyRepository extends JpaRepository<Company, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select company from Company company where company.id = :id")
    Optional<Company> findByIdForUpdate(UUID id);

    boolean existsByCnpj(String cnpj);

    Optional<Company> findByCnpj(String cnpj);

    boolean existsByAddressId(UUID addressId);

    boolean existsByBusinessContactId(UUID businessContactId);

    boolean existsBySlug(String slug);

    Optional<Company> findBySlug(String slug);

    @Override
    @EntityGraph(attributePaths = {"address", "businessContact"})
    List<Company> findAll();
}
