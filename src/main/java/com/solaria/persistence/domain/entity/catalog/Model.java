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

    @Column(name = "fk_creator_company")
    private UUID creatorCompanyId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_brand", nullable = false)
    private Brand brandEntry;

    public String getBrand() { return brandEntry == null ? null : brandEntry.getName(); }
    public UUID getBrandId() { return brandEntry == null ? null : brandEntry.getId(); }

    @Column(name = "model", nullable = false)
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
    @Column(name = "type")
    private PanelType type;

    @Column(name = "power_wp")
    private BigDecimal powerWp;

    @Column(name = "efficiency")
    private BigDecimal efficiency;

    @Column(name = "width")
    private BigDecimal width;

    @Column(name = "length")
    private BigDecimal length;

    @Column(name = "weight")
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ModelStatus status = ModelStatus.UNDER_ANALYSIS;
}
