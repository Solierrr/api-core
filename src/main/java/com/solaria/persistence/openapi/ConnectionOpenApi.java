package com.solaria.persistence.openapi;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.ConnectionRequestDTO;
import com.solaria.persistence.dto.response.UserResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Conexões", description = "Gerenciamento das conexões unilaterais do usuário autenticado")
public interface ConnectionOpenApi {

    @Operation(
        summary = "Cria uma conexão",
        description = "Conecta o usuário autenticado a outro usuário sem necessidade de aprovação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conexão criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Tentativa de auto-conexão ou dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário conectado não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conexão já existente"),
            @ApiResponse(responseCode = "422", description = "Usuário conectado inativo")
    })
    ResponseEntity<UserResponseDTO> save(ConnectionRequestDTO dto);

    @Operation(
        summary = "Lista as conexões",
        description = "Lista os dados completos dos usuários cujos IDs estão no array de conexões do usuário autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    ResponseEntity<List<UserResponseDTO>> findAll();

    @Operation(
        summary = "Busca um usuário conectado pelo id",
        description = "Retorna o usuário quando seu ID pertence ao array de conexões do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conexão encontrada"),
            @ApiResponse(responseCode = "404", description = "Conexão não encontrada")
    })
    ResponseEntity<UserResponseDTO> findById(UUID id);

    @Operation(
        summary = "Remove uma conexão",
        description = "Remove do array somente o ID do usuário conectado informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conexão removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conexão não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);
}
