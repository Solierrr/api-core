package com.solaria.persistence.domain.entity.proposal;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;
import com.solaria.persistence.domain.entity.catalog.Offer;

@Entity
@Table(name = "proposal_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProposalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_proposal", nullable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_offer", nullable = false)
    private Offer offer;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "negotiated_price")
    private BigDecimal negotiatedPrice;

    @Column(name = "discount")
    private BigDecimal discount;
}
