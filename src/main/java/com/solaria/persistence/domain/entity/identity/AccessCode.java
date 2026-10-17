package com.solaria.persistence.domain.entity.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.enums.identity.AccessCodeStatus;

@Entity
@Table(name = "access_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_company", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_position", nullable = false)
    private Position position;

    @Column(name = "code", length = 12, nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private AccessCodeStatus status = AccessCodeStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
