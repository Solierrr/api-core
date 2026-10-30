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

    @Column(name = "owner_company_id")
    private UUID ownerCompanyId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 255)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandVisibility visibility = BrandVisibility.PRIVATE;

    @Column(name = "is_archived", nullable = false)
    private boolean archived;

    @Column(name = "is_legacy", nullable = false)
    private boolean legacy;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "reference_url", length = 2048)
    private String referenceUrl;

    @Version @Column(name = "version") private long revision;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
