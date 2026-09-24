package com.solaria.persistence.domain.entity.identity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "permission_name", length = 100, nullable = false, unique = true)
    private String permissionName;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description", length = 300, nullable = false)
    private String description;
}
