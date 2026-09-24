package com.solaria.persistence.domain.entity.catalog;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "model_photo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_model", nullable = false)
    private Model model;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
