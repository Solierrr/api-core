package com.solaria.persistence.domain.entity.order;

import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "offer_slug", nullable = false, length = 160)
    private String offerSlug;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_percentage", nullable = false)
    private BigDecimal discountPercentage;

    @Column(name = "net_unit_price", nullable = false)
    private BigDecimal netUnitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total_amount", nullable = false)
    private BigDecimal lineTotal;
}
