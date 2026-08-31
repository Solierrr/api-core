package com.solaria.persistence.controller.identity;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.identity.PersonRequestDTO;
import com.solaria.persistence.dto.response.identity.PersonResponseDTO;
import com.solaria.persistence.openapi.identity.PersonOpenApi;
import com.solaria.persistence.service.identity.PersonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController implements PersonOpenApi {

    private final PersonService personService;

    @Override
    @PostMapping
    public ResponseEntity<PersonResponseDTO> save(@Valid @RequestBody PersonRequestDTO dto) {
        PersonResponseDTO response = personService.save(dto);
        return ResponseEntity.created(URI.create("/api/persons/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody PersonRequestDTO dto) {
        return ResponseEntity.ok(personService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        personService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(personService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> findAll() {
        return ResponseEntity.ok(personService.findAll());
    }
}
