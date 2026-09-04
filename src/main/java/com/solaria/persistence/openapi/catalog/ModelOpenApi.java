package com.solaria.persistence.openapi.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.dto.request.catalog.ModelRequestDTO;
import com.solaria.persistence.dto.response.catalog.ModelResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Modelos", description = "Gerenciamento de modelos de equipamentos")
public interface ModelOpenApi {

    @Operation(
        summary = "Cria um novo modelo",
        description = "Marca, modelo e medidas técnicas são obrigatórios (medidas > 0); o modelo nasce com status UNDER_ANALYSIS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Modelo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<ModelResponseDTO> save(ModelRequestDTO dto);

    @Operation(
        summary = "Atualiza um modelo existente",
        description = "Só é possível editar um modelo enquanto ele estiver com status UNDER_ANALYSIS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modelo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    })
    ResponseEntity<ModelResponseDTO> update(UUID id, ModelRequestDTO dto);

    @Operation(
        summary = "Remove um modelo",
        description = "Não é permitida a exclusão se houver Inventory ou Offer vinculados ao modelo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Modelo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um modelo pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modelo encontrado"),
            @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    })
    ResponseEntity<ModelResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os modelos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<ModelResponseDTO>> findAll();

    @Operation(summary = "Lista os modelos filtrados por status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<ModelResponseDTO>> findByStatus(ModelStatus status);

    @Operation(
        summary = "Aprova um modelo",
        description = "A aprovação é definitiva e move o modelo para fora do fluxo de análise."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modelo aprovado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    })
    ResponseEntity<ModelResponseDTO> approve(UUID id);

    @Operation(
        summary = "Rejeita um modelo",
        description = "A rejeição é definitiva e move o modelo para fora do fluxo de análise."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Modelo rejeitado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    })
    ResponseEntity<ModelResponseDTO> reject(UUID id);
}
