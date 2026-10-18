package com.solaria.persistence.repository.execution;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TechnicalProjectRepository extends JpaRepository<TechnicalProject, UUID> {

    List<TechnicalProject> findByRequester_Company_Id(UUID companyId);

    Optional<TechnicalProject> findByIdAndRequester_Company_Id(UUID id, UUID companyId);

    List<TechnicalProject> findByRequesterId(UUID requesterId);

    boolean existsByRequesterId(UUID requesterId);

 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select p from TechnicalProject p where p.id=:id")
 Optional<TechnicalProject> lockById(UUID id);
 @org.springframework.data.jpa.repository.Query("select p from TechnicalProject p where (:companyId is null or p.ownerCompanyId=:companyId) and (p.ownerUserId=:userId or (:adminCompany is not null and p.ownerCompanyId=:adminCompany) or exists(select m.id from ProjectMember m where m.projectId=p.id and m.userId=:userId and m.active=true)) order by p.createdAt desc")
 List<TechnicalProject> findAccessible(UUID userId,UUID companyId,UUID adminCompany);
}
