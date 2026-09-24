package com.solaria.persistence.openapi.proposal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.proposal.ProposalItemRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalItemResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Itens de Proposta", description = "Gerenciamento dos itens de uma proposta comercial")
public interface ProposalItemOpenApi {

    @Operation(
        summary = "Cria um novo item de proposta",
        description = "Proposta e oferta são obrigatórias e imutáveis; quantidade deve ser maior que 0; desconto entre 0% e 100%; a mesma oferta não pode repetir na mesma proposta; a oferta usada não pode estar vencida e precisa ter disponibilidade anunciada suficiente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item de proposta criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação do item de proposta")
    })
    ResponseEntity<ProposalItemResponseDTO> save(ProposalItemRequestDTO dto);

    @Operation(
        summary = "Atualiza um item de proposta existente",
        description = "Só editável enquanto a proposta estiver em negociação; quantidade deve ser maior que 0; desconto entre 0% e 100%."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item de proposta atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização do item de proposta"),
            @ApiResponse(responseCode = "404", description = "Item de proposta não encontrado")
    })
    ResponseEntity<ProposalItemResponseDTO> update(UUID id, ProposalItemRequestDTO dto);

    @Operation(
        summary = "Remove um item de proposta pelo id",
        description = "Só é possível remover enquanto a proposta estiver em negociação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item de proposta removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item de proposta não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um item de proposta pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item de proposta encontrado"),
            @ApiResponse(responseCode = "404", description = "Item de proposta não encontrado")
    })
    ResponseEntity<ProposalItemResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os itens de uma proposta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens de proposta retornada com sucesso")
    })
    ResponseEntity<List<ProposalItemResponseDTO>> findByProposal(UUID proposalId);
}
