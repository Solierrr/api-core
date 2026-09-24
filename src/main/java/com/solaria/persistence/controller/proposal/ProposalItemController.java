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

import com.solaria.persistence.dto.request.proposal.ProposalItemRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalItemResponseDTO;
import com.solaria.persistence.openapi.proposal.ProposalItemOpenApi;
import com.solaria.persistence.service.proposal.ProposalItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/proposal-items")
@RequiredArgsConstructor
public class ProposalItemController implements ProposalItemOpenApi {

    private final ProposalItemService proposalItemService;

    @Override
    @PostMapping
    public ResponseEntity<ProposalItemResponseDTO> save(@Valid @RequestBody ProposalItemRequestDTO dto) {
        ProposalItemResponseDTO response = proposalItemService.save(dto);
        return ResponseEntity.created(URI.create("/api/proposal-items/" + response.getId())).body(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ProposalItemResponseDTO> update(@PathVariable UUID id,
                                                            @Valid @RequestBody ProposalItemRequestDTO dto) {
        return ResponseEntity.ok(proposalItemService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        proposalItemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ProposalItemResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalItemService.findById(id, companyId));
    }

    @Override
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<ProposalItemResponseDTO>> findByProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(proposalItemService.findByProposal(proposalId));
    }
}
