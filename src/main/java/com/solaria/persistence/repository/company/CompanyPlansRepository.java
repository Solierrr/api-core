package com.solaria.persistence.repository.company;

import com.solaria.persistence.domain.entity.company.CompanyPlans;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface CompanyPlansRepository extends JpaRepository<CompanyPlans, UUID> {

    Optional<CompanyPlans> findByName(String name);
}
