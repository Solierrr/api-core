package com.solaria.persistence.openapi.proposal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.proposal.UpdateNotesDTO;
import com.solaria.persistence.dto.request.proposal.ProposalRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Proposals", description = "Gerenciamento do ciclo de vida das propostas comerciais, incluindo o handshake de negociação entre fornecedor e solicitante.")
public interface ProposalOpenApi {

    @Operation(
        summary = "Cria uma nova proposta comercial",
        description = "A proposta nasce no estado AWAITING_SUPPLIER com valor total vazio; o valor total é recalculado automaticamente a cada mudança de item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proposta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<ProposalResponseDTO> save(ProposalRequestDTO dto);

    @Operation(
        summary = "Atualiza as observações (notes) internas da proposta",
        description = "As notas só podem ser editadas enquanto a proposta estiver em negociação (AWAITING_SUPPLIER ou AWAITING_REQUESTER)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Observações atualizadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada")
    })
    ResponseEntity<ProposalResponseDTO> updateNotes(UUID id, UpdateNotesDTO dto);

    @Operation(
        summary = "Registra a concordância do fornecedor com os termos vigentes da proposta",
        description = "Leva a proposta a ACCEPTED, o que dispara a reserva de estoque; exige que a proposta tenha pelo menos 1 item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Concordância do fornecedor registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    ResponseEntity<ProposalResponseDTO> supplierAgree(UUID id);

    @Operation(
        summary = "Registra uma contraproposta enviada pelo fornecedor",
        description = "Move a proposta para AWAITING_REQUESTER, dando continuidade ao handshake de negociação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraproposta do fornecedor registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    ResponseEntity<ProposalResponseDTO> supplierCounter(UUID id);

    @Operation(
        summary = "Registra a concordância do solicitante com os termos vigentes da proposta",
        description = "Leva a proposta a ACCEPTED, o que dispara a reserva de estoque; exige que a proposta tenha pelo menos 1 item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Concordância do solicitante registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    ResponseEntity<ProposalResponseDTO> requesterAgree(UUID id);

    @Operation(
        summary = "Registra uma contraproposta enviada pelo solicitante",
        description = "Move a proposta para AWAITING_SUPPLIER, dando continuidade ao handshake de negociação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraproposta do solicitante registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    ResponseEntity<ProposalResponseDTO> requesterCounter(UUID id);

    @Operation(
        summary = "Rejeita a proposta, encerrando a negociação sem acordo entre as partes",
        description = "Transição definitiva: uma proposta rejeitada não pode retornar à negociação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposta rejeitada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    ResponseEntity<ProposalResponseDTO> reject(UUID id);

    @Operation(
        summary = "Cancela a proposta antes da conclusão da negociação",
        description = "Transição definitiva: uma proposta cancelada não pode retornar à negociação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposta cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Proposta em estado incompatível com esta transição")
    })
    ResponseEntity<ProposalResponseDTO> cancel(UUID id);

    @Operation(summary = "Busca uma proposta pelo identificador, escopada à empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposta encontrada"),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada")
    })
    ResponseEntity<ProposalResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista as propostas associadas a um solicitante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<ProposalResponseDTO>> findByRequester(UUID requesterId);

    @Operation(summary = "Lista todas as propostas de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<ProposalResponseDTO>> findAllByCompany(UUID companyId);
}
