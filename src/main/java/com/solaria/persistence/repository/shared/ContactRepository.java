package com.solaria.persistence.repository.shared;

import com.solaria.persistence.domain.entity.shared.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ContactRepository extends JpaRepository<Contact, UUID> {
}
