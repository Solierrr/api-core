package com.solaria.persistence.domain.entity.order;

import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.domain.enums.order.OrderPaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false, unique = true)
    private PurchaseOrder order;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderPaymentStatus status = OrderPaymentStatus.PAID;

    @Column(name = "paid_at", nullable = false)
    private OffsetDateTime paidAt = OffsetDateTime.now();

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;
}
