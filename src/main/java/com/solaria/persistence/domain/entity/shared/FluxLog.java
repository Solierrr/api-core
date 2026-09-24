package com.solaria.persistence.domain.entity.shared;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.solaria.persistence.domain.entity.identity.User;

@Entity
@Table(name = "flux_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FluxLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user", nullable = false)
    private User user;

    @Column(name = "action", nullable = false, length = 255)
    private String action;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
