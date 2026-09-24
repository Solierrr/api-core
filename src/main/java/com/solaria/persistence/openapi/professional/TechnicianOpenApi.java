package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.TechnicianRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicianResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Technicians", description = "Gerenciamento do cadastro de técnicos responsáveis pela execução de serviços.")
public interface TechnicianOpenApi {

    @Operation(
        summary = "Cadastra um novo técnico",
        description = "Pessoa e número de registro (CREA) obrigatórios; uma pessoa só pode virar técnico uma vez."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Técnico criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<TechnicianResponseDTO> save(TechnicianRequestDTO dto);

    @Operation(
        summary = "Atualiza os dados de um técnico existente",
        description = "O vínculo com a Person é imutável depois de criado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
    ResponseEntity<TechnicianResponseDTO> update(UUID id, TechnicianRequestDTO dto);

    @Operation(summary = "Remove um técnico pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Técnico removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um técnico pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico encontrado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
    ResponseEntity<TechnicianResponseDTO> findById(UUID id);

    @Operation(summary = "Lista todos os técnicos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<TechnicianResponseDTO>> findAll();
}
