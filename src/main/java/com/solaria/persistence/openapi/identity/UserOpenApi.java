package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.identity.UserRequestDTO;
import com.solaria.persistence.dto.response.identity.UserResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Usuários", description = "Gerenciamento dos usuários do sistema")
public interface UserOpenApi {

    @Operation(
        summary = "Cria um novo usuário",
        description = "O authId deve ser único; o usuário nasce sempre ativo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado para outro usuário")
    })
    ResponseEntity<UserResponseDTO> save(UserRequestDTO dto);

    @Operation(
        summary = "Atualiza um usuário existente",
        description = "O authId é imutável após a criação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    ResponseEntity<UserResponseDTO> update(UUID id, UserRequestDTO dto);

    @Operation(
        summary = "Remove um usuário",
        description = "Não é permitida a exclusão de um usuário que possua Person ou vínculo de empresa associado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um usuário pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    ResponseEntity<UserResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<UserResponseDTO>> findAll();

}
