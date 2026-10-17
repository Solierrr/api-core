package com.solaria.persistence.domain.entity.professional;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "professional_onboarding")
@Getter
@Setter
public class ProfessionalOnboarding {
    @Id
    private UUID id;
    @Column(name = "auth_id", nullable = false, unique = true)
    private UUID authId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "technician_id")
    private UUID technicianId;
    @Column(nullable = false)
    private long revision;
    @Column(name = "approved_version", nullable = false)
    private long approvedVersion;
    @Column(name = "draft_json", columnDefinition = "text", nullable = false)
    private String draftJson = "{}";
    @Column(name = "approved_json", columnDefinition = "text")
    private String approvedJson;
    @Column(nullable = false)
    private boolean suspended;
}
