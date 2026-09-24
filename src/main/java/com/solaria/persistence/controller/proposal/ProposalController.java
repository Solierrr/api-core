package com.solaria.persistence.controller.proposal;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.patch.proposal.UpdateNotesDTO;
import com.solaria.persistence.dto.request.proposal.ProposalRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalResponseDTO;
import com.solaria.persistence.openapi.proposal.ProposalOpenApi;
import com.solaria.persistence.service.proposal.ProposalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController implements ProposalOpenApi {

    private final ProposalService proposalService;

    @Override
    @PostMapping
    public ResponseEntity<ProposalResponseDTO> save(@Valid @RequestBody ProposalRequestDTO dto) {
        ProposalResponseDTO response = proposalService.save(dto);
        return ResponseEntity.created(URI.create("/api/proposals/" + response.getId())).body(response);
    }

    @Override
    @PatchMapping("/{id}/notes")
    public ResponseEntity<ProposalResponseDTO> updateNotes(@PathVariable UUID id,
                                                             @Valid @RequestBody UpdateNotesDTO dto) {
        return ResponseEntity.ok(proposalService.updateNotes(id, dto.getNotes()));
    }

    @Override
    @PostMapping("/{id}/supplier-agreement")
    public ResponseEntity<ProposalResponseDTO> supplierAgree(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.supplierAgree(id));
    }

    @Override
    @PostMapping("/{id}/supplier-counter")
    public ResponseEntity<ProposalResponseDTO> supplierCounter(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.supplierCounter(id));
    }

    @Override
    @PostMapping("/{id}/requester-agreement")
    public ResponseEntity<ProposalResponseDTO> requesterAgree(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.requesterAgree(id));
    }

    @Override
    @PostMapping("/{id}/requester-counter")
    public ResponseEntity<ProposalResponseDTO> requesterCounter(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.requesterCounter(id));
    }

    @Override
    @PostMapping("/{id}/rejection")
    public ResponseEntity<ProposalResponseDTO> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.reject(id));
    }

    @Override
    @PostMapping("/{id}/cancellation")
    public ResponseEntity<ProposalResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.cancel(id));
    }

    @Override
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ProposalResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalService.findById(id, companyId));
    }

    @Override
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<ProposalResponseDTO>> findByRequester(@PathVariable UUID requesterId) {
        return ResponseEntity.ok(proposalService.findByRequester(requesterId));
    }

    @Override
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ProposalResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalService.findAllByCompany(companyId));
    }
}
