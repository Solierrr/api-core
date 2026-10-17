package com.solaria.persistence.domain.entity.order;

import com.solaria.persistence.domain.entity.catalog.Offer;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_cart", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_offer", nullable = false)
    private Offer offer;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();
}
