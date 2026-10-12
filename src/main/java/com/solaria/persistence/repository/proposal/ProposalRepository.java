package com.solaria.persistence.repository.proposal;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.solaria.persistence.domain.entity.proposal.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProposalRepository extends JpaRepository<Proposal, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Proposal p where p.id = :id")
    Optional<Proposal> findByIdForUpdate(@Param("id") UUID id);

    @Override
    @EntityGraph(attributePaths = {"requester.company", "supplier.company"})
    Optional<Proposal> findById(UUID id);

    @EntityGraph(attributePaths = {"requester.company", "supplier.company"})
    @Query("select p from Proposal p where p.requester.company.id = :companyId "
            + "or p.supplier.company.id = :companyId")
    List<Proposal> findByParticipantCompany(@Param("companyId") UUID companyId);

    @EntityGraph(attributePaths = {"requester.company", "supplier.company"})
    @Query("select p from Proposal p where p.requester.id = :requesterId "
            + "and (p.requester.company.id = :companyId or p.supplier.company.id = :companyId)")
    List<Proposal> findByRequesterAndParticipant(@Param("requesterId") UUID requesterId,
                                              @Param("companyId") UUID companyId);

    boolean existsByRequesterId(UUID requesterId);
}
