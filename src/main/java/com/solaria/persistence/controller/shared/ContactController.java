package com.solaria.persistence.controller.shared;

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

import com.solaria.persistence.dto.request.shared.ContactRequestDTO;
import com.solaria.persistence.dto.response.shared.ContactResponseDTO;
import com.solaria.persistence.openapi.shared.ContactOpenApi;
import com.solaria.persistence.service.shared.ContactService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController implements ContactOpenApi {

    private final ContactService contactService;

    @Override
    @PostMapping
    public ResponseEntity<ContactResponseDTO> save(@Valid @RequestBody ContactRequestDTO dto) {
        ContactResponseDTO response = contactService.save(dto);
        return ResponseEntity.created(URI.create("/api/contacts/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ContactResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody ContactRequestDTO dto) {
        return ResponseEntity.ok(contactService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        contactService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(contactService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ContactResponseDTO>> findAll() {
        return ResponseEntity.ok(contactService.findAll());
    }
}
