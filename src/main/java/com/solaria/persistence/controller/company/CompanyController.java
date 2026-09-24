package com.solaria.persistence.controller.company;

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

import com.solaria.persistence.dto.patch.company.UpdateBusinessContactIdDTO;
import com.solaria.persistence.dto.patch.company.UpdateCompanyAddressIdDTO;
import com.solaria.persistence.dto.request.company.CompanyRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;
import com.solaria.persistence.openapi.company.CompanyOpenApi;
import com.solaria.persistence.service.company.CompanyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController implements CompanyOpenApi {

    private final CompanyService companyService;

    @Override
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> save(@Valid @RequestBody CompanyRequestDTO dto) {
        CompanyResponseDTO response = companyService.save(dto);
        return ResponseEntity.created(URI.create("/api/companies/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody CompanyRequestDTO dto) {
        return ResponseEntity.ok(companyService.update(id, dto));
    }

    @Override
    @PatchMapping("/{id}/address")
    public ResponseEntity<CompanyResponseDTO> attachAddress(@PathVariable UUID id,
                                                               @Valid @RequestBody UpdateCompanyAddressIdDTO dto) {
        return ResponseEntity.ok(companyService.attachAddress(id, dto.getAddressId()));
    }

    @Override
    @PatchMapping("/{id}/business-contact")
    public ResponseEntity<CompanyResponseDTO> attachBusinessContact(@PathVariable UUID id,
                                                                       @Valid @RequestBody UpdateBusinessContactIdDTO dto) {
        return ResponseEntity.ok(companyService.attachBusinessContact(id, dto.getBusinessContactId()));
    }

    @Override
    @PatchMapping("/{id}/approval")
    public ResponseEntity<CompanyResponseDTO> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.approve(id));
    }

    @Override
    @PatchMapping("/{id}/rejection")
    public ResponseEntity<CompanyResponseDTO> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.reject(id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        companyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @Override
    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<CompanyResponseDTO> findByCnpj(@PathVariable String cnpj) {
        return ResponseEntity.ok(companyService.findByCnpj(cnpj));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<CompanyResponseDTO>> findAll() {
        return ResponseEntity.ok(companyService.findAll());
    }
}
