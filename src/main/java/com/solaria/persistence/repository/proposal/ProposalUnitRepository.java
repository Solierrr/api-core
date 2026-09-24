package com.solaria.persistence.repository.proposal;

import com.solaria.persistence.domain.entity.proposal.ProposalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProposalUnitRepository extends JpaRepository<ProposalUnit, UUID> {

    Optional<ProposalUnit> findByIdAndProposalItem_Proposal_Requester_Company_Id(UUID id, UUID companyId);

    List<ProposalUnit> findByProposalItemId(UUID proposalItemId);

    boolean existsByLocalUnitId(UUID localUnitId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(pu.quantity), 0) FROM ProposalUnit pu WHERE pu.proposalItem.id = :proposalItemId")
    Integer sumQuantityByProposalItemId(@Param("proposalItemId") UUID proposalItemId);
}
