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
        summary = "Cria uma nova pessoa",
        description = "Nome, CPF e data de nascimento são obrigatórios; o CPF (11 dígitos) deve ser único e a data de nascimento precisa estar no passado; um usuário só pode ter uma Person, e o vínculo com o User é imutável."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado para outra pessoa")
    })
    ResponseEntity<PersonResponseDTO> save(PersonRequestDTO dto);

    @Operation(
        summary = "Atualiza uma pessoa existente",
        description = "O CPF deve permanecer único, a data de nascimento precisa estar no passado, e o vínculo com o User é imutável."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    ResponseEntity<PersonResponseDTO> update(UUID id, PersonRequestDTO dto);

    @Operation(
        summary = "Exclui uma pessoa pelo identificador",
        description = "Não é permitida a exclusão se a pessoa tiver um Technician vinculado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pessoa excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma pessoa pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    ResponseEntity<PersonResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todas as pessoas cadastradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pessoas retornada com sucesso")
    })
    ResponseEntity<List<PersonResponseDTO>> findAll();
}
