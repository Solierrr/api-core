package com.solaria.persistence.domain.entity.catalog;

import com.solaria.persistence.domain.enums.catalog.InfrastructureType;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.domain.enums.catalog.PanelType;
import com.solaria.persistence.domain.enums.catalog.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "model")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "creator_company_id", nullable = false, updatable = false)
    private UUID creatorCompanyId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brandEntry;

    public String getBrand() { return brandEntry == null ? null : brandEntry.getName(); }
    public UUID getBrandId() { return brandEntry == null ? null : brandEntry.getId(); }

    @Column(name = "model_name", nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType = ProductType.PANEL;

    @Enumerated(EnumType.STRING)
    @Column(name = "infrastructure_type")
    private InfrastructureType infrastructureType;

    @Column(name = "material")
    private String material;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PanelType type;

    @Column(name = "power_wp", nullable = false)
    private BigDecimal powerWp;

    @Column(name = "efficiency", nullable = false)
    private BigDecimal efficiency;

    @Column(name = "width", nullable = false)
    private BigDecimal width;

    @Column(name = "length", nullable = false)
    private BigDecimal length;

    @Column(name = "weight", nullable = false)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ModelStatus status = ModelStatus.APPROVED;
}
