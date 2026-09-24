package com.solaria.persistence.repository.proposal;

import com.solaria.persistence.domain.entity.proposal.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProposalRepository extends JpaRepository<Proposal, UUID> {

    List<Proposal> findByRequester_Company_Id(UUID companyId);

    Optional<Proposal> findByIdAndRequester_Company_Id(UUID id, UUID companyId);

    List<Proposal> findByRequesterId(UUID requesterId);


    boolean existsByRequesterId(UUID requesterId);
}
