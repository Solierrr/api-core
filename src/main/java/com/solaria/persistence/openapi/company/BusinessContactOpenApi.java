package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.company.BusinessContactRequestDTO;
import com.solaria.persistence.dto.response.company.BusinessContactResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Contatos Comerciais", description = "Gerenciamento dos contatos comerciais das empresas")
public interface BusinessContactOpenApi {

    @Operation(
        summary = "Cria um novo contato comercial",
        description = "E-mail corporativo é obrigatório; telefone é opcional (9 dígitos); site precisa ter formato de URL. Pode ser anexado a uma Company após a criação dela."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contato comercial criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<BusinessContactResponseDTO> save(BusinessContactRequestDTO dto);

    @Operation(
        summary = "Atualiza um contato comercial existente",
        description = "E-mail corporativo é obrigatório; telefone é opcional (9 dígitos); site precisa ter formato de URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contato comercial atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Contato comercial não encontrado")
    })
    ResponseEntity<BusinessContactResponseDTO> update(UUID id, BusinessContactRequestDTO dto);

    @Operation(
        summary = "Remove um contato comercial",
        description = "Não é possível excluir um contato comercial ainda referenciado por uma Company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contato comercial removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Contato comercial não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um contato comercial pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contato comercial encontrado"),
            @ApiResponse(responseCode = "404", description = "Contato comercial não encontrado")
    })
    ResponseEntity<BusinessContactResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os contatos comerciais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<BusinessContactResponseDTO>> findAll();
}
