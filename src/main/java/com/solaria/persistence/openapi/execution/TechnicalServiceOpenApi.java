package com.solaria.persistence.openapi.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.execution.ServiceAcceptanceDTO;
import com.solaria.persistence.dto.patch.execution.ServiceScheduleDTO;
import com.solaria.persistence.dto.patch.execution.UpdatePurposeDTO;
import com.solaria.persistence.dto.request.execution.TechnicalServiceRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalServiceResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Serviços Técnicos", description = "Gerenciamento dos serviços técnicos e do seu ciclo de vida")
public interface TechnicalServiceOpenApi {

    @Operation(
        summary = "Cria um novo serviço técnico",
        description = "Nasce sempre com status OPEN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço técnico criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<TechnicalServiceResponseDTO> save(TechnicalServiceRequestDTO dto);

    @Operation(
        summary = "Atualiza a finalidade de um serviço técnico",
        description = "Só é permitido alterar a finalidade enquanto o serviço estiver com status OPEN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Finalidade atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> updatePurpose(UUID id, UpdatePurposeDTO dto);

    @Operation(
        summary = "Reagenda a data de um serviço técnico",
        description = "Permitido enquanto o serviço estiver com status OPEN ou IN_PROGRESS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço técnico reagendado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> reschedule(UUID id, ServiceScheduleDTO dto);

    @Operation(
        summary = "Registra o aceite de um serviço técnico",
        description = "Transição OPEN→IN_PROGRESS; exige pelo menos um executor já cadastrado no serviço."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aceite registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> accept(UUID id, ServiceAcceptanceDTO dto);

    @Operation(
        summary = "Conclui um serviço técnico",
        description = "Só é permitido a partir do status IN_PROGRESS; COMPLETED é um status final."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço técnico concluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> complete(UUID id);

    @Operation(
        summary = "Cancela um serviço técnico",
        description = "Permitido a partir dos status OPEN ou IN_PROGRESS; CANCELED é um status final."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço técnico cancelado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> cancel(UUID id);

    @Operation(summary = "Busca um serviço técnico pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço técnico encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os serviços técnicos de um projeto técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<TechnicalServiceResponseDTO>> findByTechnicalProject(UUID technicalProjectId);

    @Operation(summary = "Lista os serviços técnicos de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<TechnicalServiceResponseDTO>> findAllByCompany(UUID companyId);
}
