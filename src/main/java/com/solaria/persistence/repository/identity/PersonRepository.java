package com.solaria.persistence.repository.identity;

import com.solaria.persistence.domain.entity.identity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

import java.util.UUID;


public interface PersonRepository extends JpaRepository<Person, UUID> {

    @Override
    @EntityGraph(attributePaths = {"user", "contact"})
    Optional<Person> findById(UUID id);

    @EntityGraph(attributePaths = {"user", "contact"})
    List<Person> findByUserId(UUID userId);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);

    boolean existsByUserId(UUID userId);

    boolean existsByContactId(UUID contactId);
}
