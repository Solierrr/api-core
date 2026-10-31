package com.solaria.persistence.domain.entity.execution;

import com.solaria.persistence.domain.enums.execution.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_photo")
@Getter
@Setter
public class ProjectPhoto {
    @Id private UUID id = UUID.randomUUID();
    @Version private long version;

    @Column(name = "technical_project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mime;

    @Column(name = "content", nullable = false, columnDefinition = "bytea")
    private byte[] content;

    @org.hibernate.annotations.Formula("octet_length(content)")
    private int size;

    @Column(name = "is_primary", nullable = false)
    private boolean cover;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedBy;

    /** Relatório de onde a imagem veio; vazio nas fotos enviadas direto para o projeto. */
    @Column(name = "report_id")
    private UUID reportId;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
