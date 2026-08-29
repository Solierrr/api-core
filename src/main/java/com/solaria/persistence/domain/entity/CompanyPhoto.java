package com.solaria.persistence.domain.entity;

import com.solaria.persistence.domain.enums.PhotoType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_photo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_company", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PhotoType type;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
