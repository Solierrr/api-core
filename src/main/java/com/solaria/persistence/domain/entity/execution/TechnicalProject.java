package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.solaria.persistence.domain.entity.unit.LocalUnit;


@Entity
@Table(name = "technical_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_requester")
    private Requester requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_local_unit")
    private LocalUnit localUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}
