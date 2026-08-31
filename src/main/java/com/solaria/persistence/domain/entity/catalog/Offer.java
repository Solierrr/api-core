package com.solaria.persistence.domain.entity.catalog;

import com.solaria.persistence.domain.enums.catalog.TranslationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_supplier", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_model", nullable = false)
    private Model model;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "availability", nullable = false)
    private Integer availability;

    @Column(name = "expiration_date")
    private OffsetDateTime expirationDate;

    @Column(name = "slug", length = 160, nullable = false, unique = true)
    private String slug;

    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;

    @ElementCollection
    @CollectionTable(name = "offer_service_region", joinColumns = @JoinColumn(name = "fk_offer"))
    @Column(name = "region", length = 120)
    private List<String> serviceRegions;

    @Column(name = "source_locale", length = 10)
    private String sourceLocale;

    @Enumerated(EnumType.STRING)
    @Column(name = "translation_status", nullable = false)
    @ColumnDefault("'PENDING'")
    private TranslationStatus translationStatus = TranslationStatus.PENDING;
}
