package com.solaria.persistence.domain.entity.order;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.order.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderStatus status = OrderStatus.PAID;

    @Column(name = "subtotal_amount", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "fee_percentage", nullable = false)
    private BigDecimal feePercentage;

    @Column(name = "fee_amount", nullable = false)
    private BigDecimal feeAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Column(name = "canceled_by_user_id")
    private UUID canceledBy;
}
