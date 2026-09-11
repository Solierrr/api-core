package com.solaria.persistence.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auth_id", nullable = false, unique = true)
    private UUID auth_id;

    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "banner")
    private String banner;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
