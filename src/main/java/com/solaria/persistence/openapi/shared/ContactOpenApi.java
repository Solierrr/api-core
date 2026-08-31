package com.solaria.persistence.openapi.shared;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.shared.ContactRequestDTO;
import com.solaria.persistence.dto.response.shared.ContactResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Contatos", description = "Gerenciamento de contatos cadastrados no sistema")
public interface ContactOpenApi {

    @Operation(
        summary = "Cria um novo contato",
        description = "E-mail é opcional, com formato válido; telefone é opcional (9 dígitos, padrão celular BR)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contato criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<ContactResponseDTO> save(ContactRequestDTO dto);

    @Operation(
        summary = "Atualiza um contato existente",
        description = "E-mail é opcional, com formato válido; telefone é opcional (9 dígitos, padrão celular BR)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contato atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Contato não encontrado")
    })
    ResponseEntity<ContactResponseDTO> update(UUID id, ContactRequestDTO dto);

    @Operation(
        summary = "Exclui um contato pelo identificador",
        description = "Não é possível excluir um contato referenciado por uma Person."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contato excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Contato não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um contato pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contato encontrado"),
            @ApiResponse(responseCode = "404", description = "Contato não encontrado")
    })
    ResponseEntity<ContactResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os contatos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de contatos retornada com sucesso")
    })
    ResponseEntity<List<ContactResponseDTO>> findAll();
}
