package com.solaria.persistence.domain.entity.proposal;

import com.solaria.persistence.domain.enums.proposal.ProposalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.solaria.persistence.domain.entity.execution.Requester;


@Entity
@Table(name = "proposal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_requester", nullable = false)
    private Requester requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProposalStatus status = ProposalStatus.AWAITING_SUPPLIER;

    @Column(name = "notes")
    private String notes;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
