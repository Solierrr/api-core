package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.UserCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCompanyRepository extends JpaRepository<UserCompany, UUID> {

    List<UserCompany> findByCompanyId(UUID companyId);

    Optional<UserCompany> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndPositionId(UUID companyId, UUID positionId);

    Optional<UserCompany> findFirstByUserIdOrderByIdAsc(UUID userId);

    default Optional<UserCompany> findByUserId(UUID userId) {
        return findFirstByUserIdOrderByIdAsc(userId);
    }

    List<UserCompany> findAllByUserId(UUID userId);

    Optional<UserCompany> findByUserIdAndCompanyId(UUID userId, UUID companyId);

    boolean existsByUserIdAndCompanyId(UUID userId, UUID companyId);

    boolean existsByUserId(UUID userId);

    boolean existsByPositionId(UUID positionId);

    boolean existsByCompanyId(UUID companyId);
}
