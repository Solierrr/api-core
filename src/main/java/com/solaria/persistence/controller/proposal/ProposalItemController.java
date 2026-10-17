package com.solaria.persistence.controller.proposal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.proposal.ProposalItemResponseDTO;
import com.solaria.persistence.service.proposal.ProposalItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@Tag(name = "Itens de Proposta", description = "Gerenciamento dos itens de uma proposta comercial")
@RestController
@RequestMapping("/api/proposal-items")
@RequiredArgsConstructor
public class ProposalItemController {

    private final ProposalItemService proposalItemService;

    @Operation(description = "Leitura compartilhada pela empresa do Requester ou Supplier persistido, com grant da rota. companyId filtra a empresa real do ator; nao concede autoridade. Nao participante recebe 404. Listas vazias de participante retornam [].", summary = "Busca um item de proposta pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Participante sem grant da rota"),
            @ApiResponse(responseCode = "200", description = "Item de proposta encontrado"),
            @ApiResponse(responseCode = "404", description = "Item de proposta não encontrado")
    })
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ProposalItemResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalItemService.findById(id, companyId));
    }

    @Operation(description = "Leitura compartilhada pela empresa do Requester ou Supplier persistido, com grant da rota. companyId filtra a empresa real do ator; nao concede autoridade. Nao participante recebe 404. Listas vazias de participante retornam [].", summary = "Lista os itens de uma proposta")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Participante sem grant da rota"),
            @ApiResponse(responseCode = "200", description = "Lista de itens de proposta retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso ou participante nao encontrado")
    })
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<ProposalItemResponseDTO>> findByProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(proposalItemService.findByProposal(proposalId));
    }
}
