package com.solaria.persistence.domain.entity.company;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "business_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_email", length = 100, nullable = false)
    private String companyEmail;

    @Column(name = "phone", length = 12)
    private String phone;

    @Column(name = "website")
    private String website;
}
