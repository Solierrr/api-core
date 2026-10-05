package com.solaria.persistence.repository.proposal;

import com.solaria.persistence.domain.entity.proposal.ProposalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProposalItemRepository extends JpaRepository<ProposalItem, UUID> {

    @Override
    @EntityGraph(attributePaths = {"proposal.requester.company", "proposal.supplier.company",
            "offer.supplier", "offer.model"})
    Optional<ProposalItem> findById(UUID id);

    @EntityGraph(attributePaths = {"proposal", "offer.supplier", "offer.model"})
    List<ProposalItem> findByProposalIdIn(List<UUID> proposalIds);

    @EntityGraph(attributePaths = {"proposal", "offer.supplier", "offer.model"})
    @Query("SELECT pi FROM ProposalItem pi WHERE pi.proposal.id = :proposalId")
    List<ProposalItem> findByProposalIdWithOffer(@Param("proposalId") UUID proposalId);

    @Query("select i.proposal.id from ProposalItem i where i.id = :id")
    Optional<UUID> findProposalIdById(@Param("id") UUID id);

    boolean existsByOfferId(UUID offerId);

}
