package com.solaria.persistence.domain.entity.execution;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "service_contract")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceContract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_service_id", nullable = false)
    private TechnicalService service;

    @Column(name = "warranty")
    private String warranty;

    @Column(name = "delivery_deadline_date")
    private LocalDate deliveryDeadline;

    @Column(name = "has_insurance", nullable = false)
    private Boolean insurance = false;

    @Column(name = "has_utility_approval", nullable = false)
    private Boolean utilityApproval = false;
}
