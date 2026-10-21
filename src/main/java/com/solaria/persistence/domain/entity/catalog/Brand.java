package com.solaria.persistence.domain.entity.catalog;

import com.solaria.persistence.domain.enums.catalog.BrandVisibility;

import jakarta.persistence.*;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "brand")
@Getter
@Setter
@NoArgsConstructor
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fk_owner_company")
    private UUID ownerCompanyId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 255)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandVisibility visibility = BrandVisibility.PRIVATE;

    @Column(nullable = false)
    private boolean archived;

    @Column(nullable = false)
    private boolean legacy;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "reference_url", length = 2048)
    private String referenceUrl;

    @Version private long revision;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
