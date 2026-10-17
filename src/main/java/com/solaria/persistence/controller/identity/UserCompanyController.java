package com.solaria.persistence.controller.identity;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.patch.identity.UpdatePositionDTO;
import com.solaria.persistence.dto.request.identity.UserCompanyRequestDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;
import com.solaria.persistence.openapi.identity.UserCompanyOpenApi;
import com.solaria.persistence.service.identity.UserCompanyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user-companies")
@RequiredArgsConstructor
public class UserCompanyController implements UserCompanyOpenApi {

    private final UserCompanyService userCompanyService;

    @Override
    @PostMapping
    public ResponseEntity<UserCompanyResponseDTO> save(@Valid @RequestBody UserCompanyRequestDTO dto) {
        UserCompanyResponseDTO response = userCompanyService.save(dto);
        return ResponseEntity.created(URI.create("/api/user-companies/" + response.getId())).body(response);
    }

    @Override
    @PatchMapping("/{id}/position")
    public ResponseEntity<UserCompanyResponseDTO> updatePosition(@PathVariable UUID id,
                                                                   @Valid @RequestBody UpdatePositionDTO dto) {
        return ResponseEntity.ok(userCompanyService.updatePosition(id, dto.getPositionId()));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        userCompanyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<UserCompanyResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(userCompanyService.findById(id, companyId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<UserCompanyResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(userCompanyService.findAllByCompany(companyId));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCompanyResponseDTO>> findByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userCompanyService.findByUser(userId));
    }
}
