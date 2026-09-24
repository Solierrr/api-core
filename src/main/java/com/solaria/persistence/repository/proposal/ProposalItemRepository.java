package com.solaria.persistence.repository.proposal;

import com.solaria.persistence.domain.entity.proposal.ProposalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProposalItemRepository extends JpaRepository<ProposalItem, UUID> {

    Optional<ProposalItem> findByIdAndProposal_Requester_Company_Id(UUID id, UUID companyId);

    List<ProposalItem> findByProposalId(UUID proposalId);

    @Query("SELECT pi FROM ProposalItem pi JOIN FETCH pi.offer WHERE pi.proposal.id = :proposalId")
    List<ProposalItem> findByProposalIdWithOffer(@Param("proposalId") UUID proposalId);

    boolean existsByOfferId(UUID offerId);

    boolean existsByProposalIdAndOfferId(UUID proposalId, UUID offerId);
}
