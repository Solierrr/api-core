package com.solaria.persistence.domain.entity.order;

import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.domain.enums.order.OrderPaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JoinColumn(name = "fk_order", nullable = false, unique = true)
    private PurchaseOrder order;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
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
