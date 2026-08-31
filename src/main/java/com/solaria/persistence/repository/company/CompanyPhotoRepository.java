package com.solaria.persistence.repository.company;

import com.solaria.persistence.domain.entity.company.CompanyPhoto;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CompanyPhotoRepository extends JpaRepository<CompanyPhoto, UUID> {

    List<CompanyPhoto> findByCompanyIdOrderByTypeAsc(UUID companyId);

    Optional<CompanyPhoto> findByCompanyIdAndType(UUID companyId, PhotoType type);

    boolean existsByCompanyId(UUID companyId);
}
