package com.solaria.persistence.domain.entity.execution;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import com.solaria.persistence.domain.entity.company.Company;


@Entity
@Table(name = "requester")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Requester {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_company", nullable = false)
    private Company company;

    @Column(name = "business_type", length = 40)
    private String businessType;
}
