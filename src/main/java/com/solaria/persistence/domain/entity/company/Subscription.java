package com.solaria.persistence.domain.entity.company;

import com.solaria.persistence.domain.enums.company.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.solaria.persistence.domain.entity.catalog.Supplier;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_plan_id", nullable = false)
    private CompanyPlans plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PAID;

    @Column(name = "has_auto_renewal", nullable = false)
    private Boolean autoRenewal = true;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "ends_at")
    private OffsetDateTime endDate;
}
