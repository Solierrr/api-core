package com.solaria.persistence.controller.identity;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.identity.AccessCodeRequestDTO;
import com.solaria.persistence.dto.request.identity.RedeemAccessCodeRequestDTO;
import com.solaria.persistence.dto.response.identity.AccessCodeResponseDTO;
import com.solaria.persistence.dto.response.identity.UserCompanyResponseDTO;
import com.solaria.persistence.service.identity.AccessCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/access-codes")
@RequiredArgsConstructor
public class AccessCodeController {

    private final AccessCodeService accessCodeService;

    @PostMapping
    public ResponseEntity<AccessCodeResponseDTO> generate(@Valid @RequestBody AccessCodeRequestDTO dto) {
        AccessCodeResponseDTO response = accessCodeService.generate(dto);
        return ResponseEntity.created(URI.create("/api/access-codes/" + response.getId())).body(response);
    }

    @PostMapping("/redeem")
    public ResponseEntity<UserCompanyResponseDTO> redeem(@Valid @RequestBody RedeemAccessCodeRequestDTO dto) {
        return ResponseEntity.ok(accessCodeService.redeem(dto.getCode()));
    }

    @DeleteMapping("/{id}/company/{companyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, @PathVariable UUID companyId) {
        accessCodeService.revoke(id, companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<AccessCodeResponseDTO>> findByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(accessCodeService.findByCompany(companyId));
    }
}
