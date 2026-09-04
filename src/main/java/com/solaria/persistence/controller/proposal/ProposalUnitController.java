package com.solaria.persistence.controller.proposal;

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

import com.solaria.persistence.dto.request.proposal.ProposalUnitRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalUnitResponseDTO;
import com.solaria.persistence.openapi.proposal.ProposalUnitOpenApi;
import com.solaria.persistence.service.proposal.ProposalUnitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/proposal-units")
@RequiredArgsConstructor
public class ProposalUnitController implements ProposalUnitOpenApi {

    private final ProposalUnitService proposalUnitService;

    @Override
    @PostMapping
    public ResponseEntity<ProposalUnitResponseDTO> save(@Valid @RequestBody ProposalUnitRequestDTO dto) {
        ProposalUnitResponseDTO response = proposalUnitService.save(dto);
        return ResponseEntity.created(URI.create("/api/proposal-units/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ProposalUnitResponseDTO> update(@PathVariable UUID id,
                                                            @Valid @RequestBody ProposalUnitRequestDTO dto) {
        return ResponseEntity.ok(proposalUnitService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        proposalUnitService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ProposalUnitResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalUnitService.findById(id, companyId));
    }

    @Override
    @GetMapping("/proposal-item/{proposalItemId}")
    public ResponseEntity<List<ProposalUnitResponseDTO>> findByProposalItem(@PathVariable UUID proposalItemId) {
        return ResponseEntity.ok(proposalUnitService.findByProposalItem(proposalItemId));
    }
}
