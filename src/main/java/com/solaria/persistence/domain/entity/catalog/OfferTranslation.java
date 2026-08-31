package com.solaria.persistence.domain.entity.catalog;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "offer_translation", uniqueConstraints = @UniqueConstraint(columnNames = {"fk_offer", "locale"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_offer", nullable = false)
    private Offer offer;

    @Column(name = "locale", length = 10, nullable = false)
    private String locale;

    @Column(name = "title", length = 160, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text", nullable = false)
    private String description;

    @Column(name = "details", columnDefinition = "text")
    private String details;
}
