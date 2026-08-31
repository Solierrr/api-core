package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface PersonRepository extends JpaRepository<Person, UUID> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);

    boolean existsByUserId(UUID userId);

    boolean existsByContactId(UUID contactId);
}
