package com.solaria.persistence.domain.entity.proposal;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import com.solaria.persistence.domain.entity.unit.LocalUnit;

@Entity
@Table(name = "proposal_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProposalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_proposal_item", nullable = false)
    private ProposalItem proposalItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_local_unit", nullable = false)
    private LocalUnit localUnit;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "note")
    private String note;
}
