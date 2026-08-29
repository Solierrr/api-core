package com.solaria.persistence.domain.entity;

import com.solaria.persistence.domain.enums.ModelStatus;
import com.solaria.persistence.domain.enums.PanelType;
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

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "model", nullable = false)
    private String model;

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
    private ModelStatus status = ModelStatus.UNDER_ANALYSIS;
}
