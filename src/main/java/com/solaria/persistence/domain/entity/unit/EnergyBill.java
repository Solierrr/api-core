package com.solaria.persistence.domain.entity.unit;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "energy_bill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnergyBill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_local_unit", nullable = false)
    private LocalUnit localUnit;

    @Column(name = "consumption", nullable = false)
    private BigDecimal consumption;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "photo_public_id")
    private String photoPublicId;
}
