package com.solaria.persistence.repository.proposal;

import org.springframework.data.jpa.repository.Query;
import com.solaria.persistence.domain.entity.proposal.ProposalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProposalUnitRepository extends JpaRepository<ProposalUnit, UUID> {

    @Override
    @EntityGraph(attributePaths = {"proposalItem.proposal.requester.company",
            "proposalItem.proposal.supplier.company", "localUnit.requester", "localUnit.address.ownerCompany"})
    Optional<ProposalUnit> findById(UUID id);

    @EntityGraph(attributePaths = {"proposalItem.proposal.requester.company",
            "proposalItem.proposal.supplier.company", "localUnit.requester", "localUnit.address.ownerCompany"})
    List<ProposalUnit> findByProposalItemId(UUID proposalItemId);

    @Query("select u.proposalItem.id, sum(u.quantity) "
            + "from ProposalUnit u where u.proposalItem.id in :ids group by u.proposalItem.id")
    List<Object[]> sumQuantitiesByItemIds(@Param("ids") List<UUID> ids);

    List<ProposalUnit> findByProposalItemIdIn(List<UUID> ids);

    @Query("select u.proposalItem.proposal.id from ProposalUnit u where u.id = :id")
    Optional<UUID> findProposalIdById(@Param("id") UUID id);

    boolean existsByLocalUnitId(UUID localUnitId);

    @Query("SELECT COALESCE(SUM(pu.quantity), 0) FROM ProposalUnit pu WHERE pu.proposalItem.id = :proposalItemId")
    Long sumQuantityByProposalItemId(@Param("proposalItemId") UUID proposalItemId);
}
