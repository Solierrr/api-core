package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.ProfessionalReviewRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionalReviewResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Avaliações de Profissionais", description = "Gerenciamento das avaliações realizadas sobre profissionais")
public interface ProfessionalReviewOpenApi {

    @Operation(
        summary = "Cria uma nova avaliação de profissional",
        description = "A nota deve estar entre 0 e 5; só pode ser criada se o TechnicalService avaliado estiver COMPLETED e o profissional avaliado tiver realmente executado aquele serviço; um avaliador não pode avaliar o mesmo profissional para o mesmo serviço mais de uma vez."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<ProfessionalReviewResponseDTO> save(ProfessionalReviewRequestDTO dto);

    @Operation(
        summary = "Desativa uma avaliação de profissional",
        description = "Não há exclusão física de avaliações, apenas desativação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    ResponseEntity<ProfessionalReviewResponseDTO> deactivate(UUID id);

    @Operation(summary = "Busca uma avaliação de profissional pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação encontrada"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    ResponseEntity<ProfessionalReviewResponseDTO> findById(UUID id);

    @Operation(summary = "Lista as avaliações de um profissional")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<ProfessionalReviewResponseDTO>> findByProfessional(UUID professionalId);
}
