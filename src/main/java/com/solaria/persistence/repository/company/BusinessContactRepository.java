package com.solaria.persistence.repository.company;

import com.solaria.persistence.domain.entity.company.BusinessContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BusinessContactRepository extends JpaRepository<BusinessContact, UUID> {
}
