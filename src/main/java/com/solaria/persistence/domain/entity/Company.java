package com.solaria.persistence.domain.entity;

import com.solaria.persistence.domain.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'UNDER_ANALYSIS'")
    private CompanyStatus status = CompanyStatus.UNDER_ANALYSIS;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_address")
    private Address address;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_business_contact")
    private BusinessContact businessContact;

    @Column(name = "cnpj", length = 14, nullable = false)
    private String cnpj;

    @Column(name = "trade_name", length = 120, nullable = false)
    private String tradeName;

    @Column(name = "corporate_name", length = 120, nullable = false)
    private String corporateName;

    @Column(name = "slug", length = 160, nullable = false, unique = true)
    private String slug;
}
