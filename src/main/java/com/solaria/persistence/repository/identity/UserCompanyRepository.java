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

    Optional<UserCompany> findByUserId(UUID userId);

    default List<UserCompany> findAllByUserId(UUID userId) {
        return findByUserId(userId).map(List::of).orElseGet(List::of);
    }

    default Optional<UserCompany> findByUserIdAndCompanyId(UUID userId, UUID companyId) {
        return findByUserId(userId).filter(link -> link.getCompany().getId().equals(companyId));
    }

    boolean existsByUserId(UUID userId);

    boolean existsByPositionId(UUID positionId);

    boolean existsByCompanyId(UUID companyId);

    boolean existsByCompanyIdAndPosition_NameAndIdNot(UUID companyId, String name, UUID id);

    boolean existsByUserIdAndCompanyIdAndPosition_Name(UUID userId, UUID companyId, String name);
}
