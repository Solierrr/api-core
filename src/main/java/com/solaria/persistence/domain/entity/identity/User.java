package com.solaria.persistence.domain.entity.identity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auth_id", nullable = false, unique = true)
    private UUID authId;

    @Column(name = "managed_by_company_id")
    private UUID managedByCompanyId;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "banner")
    private String banner;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "connection_user_ids", nullable = false)
    private List<UUID> connections = new ArrayList<>();
}
