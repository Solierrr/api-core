package com.solaria.persistence.domain.entity.professional;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import com.solaria.persistence.domain.entity.company.Company;

@Entity
@Table(name = "technical_course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_company")
    private Company company;

    @Column(name = "title", length = 30)
    private String title;

    @Column(name = "information", columnDefinition = "TEXT")
    private String information;

    @Column(name = "link", columnDefinition = "TEXT")
    private String link;
}
