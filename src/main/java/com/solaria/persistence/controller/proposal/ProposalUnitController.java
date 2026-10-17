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
import com.solaria.persistence.service.proposal.ProposalUnitService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;


@Tag(name = "Unidades de Proposta", description = "Gerenciamento das unidades/itens que compõem uma proposta comercial")
@RestController
@RequestMapping("/api/proposal-units")
@RequiredArgsConstructor
public class ProposalUnitController {

    private final ProposalUnitService proposalUnitService;

    @Operation(
        summary = "Cria uma nova unidade de proposta",
        description = "Item de proposta e unidade local são obrigatórios; quantidade deve ser maior que 0; a unidade precisa pertencer ao mesmo solicitante dono da proposta; a soma alocada não pode ultrapassar a quantidade do item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou ator não é Requester"),
            @ApiResponse(responseCode = "409", description = "Proposal em estado terminal"),
            @ApiResponse(responseCode = "201", description = "Unidade de proposta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<ProposalUnitResponseDTO> save(@Valid @RequestBody ProposalUnitRequestDTO dto) {
        ProposalUnitResponseDTO response = proposalUnitService.save(dto);
        return ResponseEntity.created(URI.create("/api/proposal-units/" + response.getId())).body(response);
    }

    @Operation(
        summary = "Atualiza uma unidade de proposta existente",
        description = "Só Requester em ambos turnos pendentes, sem mudar a vez; quantidade deve ser maior que 0; a soma alocada não pode ultrapassar a quantidade do item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou ator não é Requester"),
            @ApiResponse(responseCode = "409", description = "Proposal em estado terminal"),
            @ApiResponse(responseCode = "200", description = "Unidade de proposta atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Unidade de proposta não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProposalUnitResponseDTO> update(@PathVariable UUID id,
                                                            @Valid @RequestBody ProposalUnitRequestDTO dto) {
        return ResponseEntity.ok(proposalUnitService.update(id, dto));
    }

    @Operation(
        summary = "Exclui uma unidade de proposta pelo identificador",
        description = "Só Requester em ambos turnos pendentes, sem mudar a vez."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou ator não é Requester"),
            @ApiResponse(responseCode = "409", description = "Proposal em estado terminal"),
            @ApiResponse(responseCode = "204", description = "Unidade de proposta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade de proposta não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        proposalUnitService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Leitura compartilhada pela empresa do Requester ou Supplier persistido, com grant da rota. companyId filtra a empresa real do ator; nao concede autoridade. Nao participante recebe 404. Listas vazias de participante retornam [].", summary = "Busca uma unidade de proposta pelo identificador e empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Participante sem grant da rota"),
            @ApiResponse(responseCode = "200", description = "Unidade de proposta encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade de proposta não encontrada")
    })
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ProposalUnitResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalUnitService.findById(id, companyId));
    }

    @Operation(description = "Leitura compartilhada pela empresa do Requester ou Supplier persistido, com grant da rota. companyId filtra a empresa real do ator; nao concede autoridade. Nao participante recebe 404. Listas vazias de participante retornam [].", summary = "Lista as unidades de um item de proposta")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Participante sem grant da rota"),
            @ApiResponse(responseCode = "200", description = "Lista de unidades de proposta retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso ou participante nao encontrado")
    })
    @GetMapping("/proposal-item/{proposalItemId}")
    public ResponseEntity<List<ProposalUnitResponseDTO>> findByProposalItem(@PathVariable UUID proposalItemId) {
        return ResponseEntity.ok(proposalUnitService.findByProposalItem(proposalItemId));
    }
}
