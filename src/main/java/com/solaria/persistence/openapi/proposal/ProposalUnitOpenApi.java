package com.solaria.persistence.openapi.proposal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.proposal.ProposalUnitRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalUnitResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Unidades de Proposta", description = "Gerenciamento das unidades/itens que compõem uma proposta comercial")
public interface ProposalUnitOpenApi {

    @Operation(
        summary = "Cria uma nova unidade de proposta",
        description = "Item de proposta e unidade local são obrigatórios; quantidade deve ser maior que 0; a unidade precisa pertencer ao mesmo solicitante dono da proposta; a soma alocada não pode ultrapassar a quantidade do item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidade de proposta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<ProposalUnitResponseDTO> save(ProposalUnitRequestDTO dto);

    @Operation(
        summary = "Atualiza uma unidade de proposta existente",
        description = "Só editável durante a negociação; quantidade deve ser maior que 0; a soma alocada não pode ultrapassar a quantidade do item."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade de proposta atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Unidade de proposta não encontrada")
    })
    ResponseEntity<ProposalUnitResponseDTO> update(UUID id, ProposalUnitRequestDTO dto);

    @Operation(
        summary = "Exclui uma unidade de proposta pelo identificador",
        description = "Só é possível excluir enquanto a proposta estiver em negociação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unidade de proposta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade de proposta não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma unidade de proposta pelo identificador e empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade de proposta encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade de proposta não encontrada")
    })
    ResponseEntity<ProposalUnitResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista as unidades de um item de proposta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de unidades de proposta retornada com sucesso")
    })
    ResponseEntity<List<ProposalUnitResponseDTO>> findByProposalItem(UUID proposalItemId);
}
