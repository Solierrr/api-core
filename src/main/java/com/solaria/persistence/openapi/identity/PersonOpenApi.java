package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.identity.PersonRequestDTO;
import com.solaria.persistence.dto.response.identity.PersonResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pessoas", description = "Gerenciamento de pessoas físicas cadastradas no sistema")
public interface PersonOpenApi {

    @Operation(
        summary = "Cria a pessoa do titular com contato inline",
        description = "User vem do JWT. Nome, CPF de 11 dígitos e nascimento passado são obrigatórios. Contato inline opcional é criado na mesma transação; cada titular tem uma Person."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário autenticado não provisionado"),
            @ApiResponse(responseCode = "409", description = "CPF ou usuário já cadastrado")
    })
    ResponseEntity<PersonResponseDTO> save(PersonRequestDTO dto);

    @Operation(
        summary = "Atualiza a pessoa do titular existente",
        description = "CPF e titular são imutáveis. Contato inline mantém o ID atual; ausente ou null remove o contato na mesma transação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Recurso de outro titular"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    ResponseEntity<PersonResponseDTO> update(UUID id, PersonRequestDTO dto);

    @Operation(
        summary = "Exclui uma pessoa pelo identificador",
        description = "Não é permitida a exclusão se a pessoa tiver um Technician vinculado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pessoa excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Recurso de outro titular"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma pessoa pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
            @ApiResponse(responseCode = "403", description = "Recurso de outro titular"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    ResponseEntity<PersonResponseDTO> findById(UUID id);

    @Operation(summary = "Lista somente a pessoa do titular")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pessoas retornada com sucesso")
    })
    ResponseEntity<List<PersonResponseDTO>> findAll();
}
