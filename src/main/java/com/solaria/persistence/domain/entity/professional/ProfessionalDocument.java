package com.solaria.persistence.domain.entity.professional;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "professional_document")
@Getter
@Setter
public class ProfessionalDocument {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 20)
    private String kind;
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;
    @Column(nullable = false, length = 64)
    private String sha256;
    @Column(columnDefinition = "bytea")
    private byte[] content;
    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;
    @Column(name = "cloudinary_format", length = 20)
    private String cloudinaryFormat;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
