package com.solaria.persistence.domain.entity.identity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;
import com.solaria.persistence.domain.entity.shared.Contact;

@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_users")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_contact")
    private Contact contact;

    @Column(name = "name", length = 60, nullable = false)
    private String name;

    @Column(name = "cpf", length = 11, nullable = false)
    private String cpf;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
}
