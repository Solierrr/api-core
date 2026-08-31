package com.solaria.persistence.controller.unit;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.patch.unit.UpdateLocalUnitAddressIdDTO;
import com.solaria.persistence.dto.request.unit.LocalUnitRequestDTO;
import com.solaria.persistence.dto.response.unit.LocalUnitResponseDTO;
import com.solaria.persistence.openapi.unit.LocalUnitOpenApi;
import com.solaria.persistence.service.unit.LocalUnitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/local-units")
@RequiredArgsConstructor
public class LocalUnitController implements LocalUnitOpenApi {

    private final LocalUnitService localUnitService;

    @Override
    @PostMapping
    public ResponseEntity<LocalUnitResponseDTO> save(@Valid @RequestBody LocalUnitRequestDTO dto) {
        LocalUnitResponseDTO response = localUnitService.save(dto);
        return ResponseEntity.created(URI.create("/api/local-units/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LocalUnitResponseDTO> update(@PathVariable UUID id,
                                                         @Valid @RequestBody LocalUnitRequestDTO dto) {
        return ResponseEntity.ok(localUnitService.update(id, dto));
    }

    @Override
    @PatchMapping("/{id}/address")
    public ResponseEntity<LocalUnitResponseDTO> attachAddress(@PathVariable UUID id,
                                                                 @Valid @RequestBody UpdateLocalUnitAddressIdDTO dto) {
        return ResponseEntity.ok(localUnitService.attachAddress(id, dto.getAddressId()));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        localUnitService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<LocalUnitResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(localUnitService.findById(id, companyId));
    }

    @Override
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<LocalUnitResponseDTO>> findByRequester(@PathVariable UUID requesterId) {
        return ResponseEntity.ok(localUnitService.findByRequester(requesterId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<LocalUnitResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(localUnitService.findAllByCompany(companyId));
    }
}
