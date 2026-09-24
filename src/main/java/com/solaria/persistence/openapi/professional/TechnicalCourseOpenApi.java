package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.TechnicalCourseRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicalCourseResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cursos Técnicos", description = "Gerenciamento dos cursos técnicos cadastrados")
public interface TechnicalCourseOpenApi {

    @Operation(
        summary = "Cria um novo curso técnico",
        description = "O vínculo com a empresa (Company) é opcional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Curso técnico criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação do curso técnico")
    })
    ResponseEntity<TechnicalCourseResponseDTO> save(TechnicalCourseRequestDTO dto);

    @Operation(
        summary = "Atualiza um curso técnico existente",
        description = "O vínculo com a empresa (Company) pode ser trocado ou removido livremente a qualquer momento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curso técnico atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização do curso técnico"),
            @ApiResponse(responseCode = "404", description = "Curso técnico não encontrado")
    })
    ResponseEntity<TechnicalCourseResponseDTO> update(UUID id, TechnicalCourseRequestDTO dto);

    @Operation(
        summary = "Remove um curso técnico pelo id",
        description = "Não verifica dependências antes de excluir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curso técnico removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Curso técnico não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um curso técnico pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curso técnico encontrado"),
            @ApiResponse(responseCode = "404", description = "Curso técnico não encontrado")
    })
    ResponseEntity<TechnicalCourseResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os cursos técnicos de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cursos técnicos retornada com sucesso")
    })
    ResponseEntity<List<TechnicalCourseResponseDTO>> findByCompany(UUID companyId);
}
