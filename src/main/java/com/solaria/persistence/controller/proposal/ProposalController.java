package com.solaria.persistence.controller.proposal;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.request.proposal.ProposalTermsRequestDTO;
import com.solaria.persistence.dto.request.proposal.ProposalRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalResponseDTO;
import com.solaria.persistence.service.proposal.ProposalService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;


@Tag(name = "Proposals", description = "Gerenciamento do ciclo de vida das propostas comerciais, incluindo o handshake de negociação entre fornecedor e solicitante.")
@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @Operation(
        summary = "Cria uma nova proposta comercial",
        description = "Contratante derivada da empresa do ator envia Supplier e itens completos em uma transação. A proposta nasce AWAITING_SUPPLIER, com preços e descontos materializados, total calculado pelo servidor e itens na resposta. Não reserva estoque na criação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proposta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Corpo ou termos comerciais inválidos"),
            @ApiResponse(responseCode = "401", description = "Credencial ausente ou inválida"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou ator sem Requester"),
            @ApiResponse(responseCode = "404", description = "Supplier ou Offer não encontrado"),
            @ApiResponse(responseCode = "409", description = "Offer repetida no envio"),
            @ApiResponse(responseCode = "422", description = "Offer expirada ou sem disponibilidade suficiente")
    })
    @PostMapping
    public ResponseEntity<ProposalResponseDTO> save(@Valid @RequestBody ProposalRequestDTO dto) {
        ProposalResponseDTO response = proposalService.save(dto);
        return ResponseEntity.created(URI.create("/api/proposals/" + response.getId())).body(response);
    }

    @Operation(summary = "Envia o snapshot comercial e transfere a vez",
            description = "Parte aguardada envia items completos não vazios e notes opcionais. IDs sobreviventes e Offers são preservados, preço sobrevivente é explícito. Omitidos removem alocações na mesma transação. No-op válido transfere a vez. Sem nova versão ou rascunho.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Termos gravados e vez transferida"),
            @ApiResponse(responseCode = "400", description = "Campos, identidade de Item ou preço sobrevivente inválidos"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente"),
            @ApiResponse(responseCode = "404", description = "Recurso inexistente ou não participante"),
            @ApiResponse(responseCode = "409", description = "Vez/estado incompatível ou identidade/Offer repetida"),
            @ApiResponse(responseCode = "422", description = "Offer indisponível ou quantidade abaixo das alocações")
    })
    @PutMapping("/{id}/terms")
    public ResponseEntity<ProposalResponseDTO> updateTerms(@PathVariable UUID id,
                                                         @Valid @RequestBody ProposalTermsRequestDTO dto) {
        return ResponseEntity.ok(proposalService.updateTerms(id, dto));
    }

    @Operation(summary = "Aceita os termos pela parte aguardada",
            description = "Serializa a Proposal e consome disponibilidade corrente com Inventory protegido. Conflito reverte integralmente o aceite. Sem body obrigatório.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposta aceita e estoque reservado"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente"),
            @ApiResponse(responseCode = "404", description = "Recurso inexistente ou não participante"),
            @ApiResponse(responseCode = "409", description = "Vez, estado ou saldo corrente incompatível"),
            @ApiResponse(responseCode = "422", description = "Proposta sem itens")
    })
    @PostMapping("/{id}/acceptance")
    public ResponseEntity<ProposalResponseDTO> accept(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.accept(id));
    }

    @Operation(
        summary = "Rejeita a proposta, encerrando a negociação sem acordo entre as partes",
        description = "Somente Supplier em qualquer turno pendente; terminal409, não participante404, grant ou papel errado403."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposta rejeitada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou ator não é Supplier"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    @PostMapping("/{id}/rejection")
    public ResponseEntity<ProposalResponseDTO> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.reject(id));
    }

    @Operation(
        summary = "Cancela a proposta antes da conclusão da negociação",
        description = "Somente Requester em qualquer turno pendente; terminal409, não participante404, grant ou papel errado403."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposta cancelada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou ator não é Requester"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    @PostMapping("/{id}/cancellation")
    public ResponseEntity<ProposalResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(proposalService.cancel(id));
    }

    @Operation(description = "Leitura compartilhada pela empresa do Requester ou Supplier persistido, com grant da rota. companyId filtra a empresa real do ator; nao concede autoridade. Nao participante recebe 404. Listas vazias de participante retornam [].", summary = "Busca uma proposta pelo identificador, escopada à empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Participante sem grant da rota"),
            @ApiResponse(responseCode = "200", description = "Proposta encontrada"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada")
    })
    @GetMapping("/{id}/company/{companyId}")
    public ResponseEntity<ProposalResponseDTO> findById(@PathVariable UUID id, @PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalService.findById(id, companyId));
    }

    @Operation(description = "Leitura compartilhada pela empresa do Requester ou Supplier persistido, com grant da rota. companyId filtra a empresa real do ator; nao concede autoridade. Nao participante recebe 404. Listas vazias de participante retornam [].", summary = "Lista as propostas associadas a um solicitante")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Participante sem grant da rota"),
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso ou participante nao encontrado")
    })
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<ProposalResponseDTO>> findByRequester(@PathVariable UUID requesterId) {
        return ResponseEntity.ok(proposalService.findByRequester(requesterId));
    }

    @Operation(description = "Leitura compartilhada pela empresa do Requester ou Supplier persistido, com grant da rota. companyId filtra a empresa real do ator; nao concede autoridade. Nao participante recebe 404. Listas vazias de participante retornam [].", summary = "Lista todas as propostas de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Participante sem grant da rota"),
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso ou participante nao encontrado")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ProposalResponseDTO>> findAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(proposalService.findAllByCompany(companyId));
    }
}
