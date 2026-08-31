package com.solaria.persistence.openapi.shared;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.shared.AddressRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Endereços", description = "Gerenciamento de endereços cadastrados no sistema")
public interface AddressOpenApi {

    @Operation(
        summary = "Cria um novo endereço",
        description = "CEP é obrigatório e deve ter 8 dígitos numéricos (validado no DTO e reforçado por trigger); estado deve ter 2 caracteres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<AddressResponseDTO> save(AddressRequestDTO dto);

    @Operation(
        summary = "Atualiza um endereço existente",
        description = "CEP é obrigatório e deve ter 8 dígitos numéricos (validado no DTO e reforçado por trigger); estado deve ter 2 caracteres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    ResponseEntity<AddressResponseDTO> update(UUID id, AddressRequestDTO dto);

    @Operation(
        summary = "Exclui um endereço pelo identificador",
        description = "Não é possível excluir um endereço referenciado por Company, LocalUnit ou Geolocalization."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Endereço excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um endereço pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    ResponseEntity<AddressResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os endereços cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de endereços retornada com sucesso")
    })
    ResponseEntity<List<AddressResponseDTO>> findAll();
}
