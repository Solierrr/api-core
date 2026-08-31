package com.solaria.persistence.domain.entity.execution;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.solaria.persistence.domain.entity.professional.TechnicianAffiliation;

@Entity
@Table(name = "service_executor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceExecutor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_service", nullable = false)
    private TechnicalService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_technician_affiliation", nullable = false)
    private TechnicianAffiliation technicianAffiliation;

    @Column(name = "function", nullable = false)
    private String function;
}
