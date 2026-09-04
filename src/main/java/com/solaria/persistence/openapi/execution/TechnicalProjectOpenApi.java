package com.solaria.persistence.openapi.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.execution.TechnicalProjectRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalProjectResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Projetos Técnicos", description = "Gerenciamento dos projetos técnicos das empresas")
public interface TechnicalProjectOpenApi {

    @Operation(
        summary = "Cria um novo projeto técnico",
        description = "Solicitante e unidade local são opcionais."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto técnico criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<TechnicalProjectResponseDTO> save(TechnicalProjectRequestDTO dto);

    @Operation(
        summary = "Atualiza um projeto técnico existente",
        description = "Solicitante e unidade local são opcionais."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto técnico atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Projeto técnico não encontrado")
    })
    ResponseEntity<TechnicalProjectResponseDTO> update(UUID id, TechnicalProjectRequestDTO dto);

    @Operation(
        summary = "Exclui um projeto técnico pelo identificador",
        description = "Não pode ser excluído enquanto tiver algum TechnicalService em aberto (OPEN) ou em andamento (IN_PROGRESS)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto técnico excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Projeto técnico não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(
        summary = "Busca um projeto técnico pelo identificador e empresa",
        description = "Consulta restrita à empresa dona do projeto (isolamento multi-tenant)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto técnico encontrado"),
            @ApiResponse(responseCode = "404", description = "Projeto técnico não encontrado")
    })
    ResponseEntity<TechnicalProjectResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os projetos técnicos de um solicitante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de projetos técnicos retornada com sucesso")
    })
    ResponseEntity<List<TechnicalProjectResponseDTO>> findByRequester(UUID requesterId);

    @Operation(
        summary = "Lista todos os projetos técnicos de uma empresa",
        description = "Consulta restrita à empresa dona dos projetos (isolamento multi-tenant)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de projetos técnicos retornada com sucesso")
    })
    ResponseEntity<List<TechnicalProjectResponseDTO>> findAllByCompany(UUID companyId);
}
