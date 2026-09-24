package com.solaria.persistence.repository.company;

import com.solaria.persistence.domain.entity.company.CompanyPlans;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface CompanyPlansRepository extends JpaRepository<CompanyPlans, UUID> {
}
