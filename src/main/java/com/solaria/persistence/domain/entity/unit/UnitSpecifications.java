package com.solaria.persistence.domain.entity.unit;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "unit_specification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitSpecifications {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_unit_id", nullable = false)
    private LocalUnit localUnit;

    @Column(name = "specifications")
    private String specifications;

    @Column(name = "location_photos")
    private String locationPhotos;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;
}
