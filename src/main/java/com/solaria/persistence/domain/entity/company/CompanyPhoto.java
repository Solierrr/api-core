package com.solaria.persistence.domain.entity.company;

import com.solaria.persistence.domain.enums.shared.PhotoType;
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

    @Column(name = "crop_x")
    private Integer cropX;

    @Column(name = "crop_y")
    private Integer cropY;

    @Column(name = "crop_width")
    private Integer cropWidth;

    @Column(name = "crop_height")
    private Integer cropHeight;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
