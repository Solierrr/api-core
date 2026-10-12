package com.solaria.persistence.openapi.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.execution.ServiceAcceptanceDTO;
import com.solaria.persistence.dto.patch.execution.ServiceScheduleDTO;
import com.solaria.persistence.dto.patch.execution.UpdatePurposeDTO;
import com.solaria.persistence.dto.request.execution.TechnicalServiceRequestDTO;
import com.solaria.persistence.dto.response.execution.TechnicalServiceResponseDTO;
import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceResponseDTO;

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
            @ApiResponse(responseCode = "404", description = "Recurso ou parent inexistente"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou recurso de outra empresa"),
            @ApiResponse(responseCode = "201", description = "Serviço técnico criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<TechnicalServiceResponseDTO> save(TechnicalServiceRequestDTO dto);

    @Operation(
        summary = "Atualiza a finalidade de um serviço técnico",
        description = "Só é permitido alterar a finalidade enquanto o serviço estiver com status OPEN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou recurso de outra empresa"),
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
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou recurso de outra empresa"),
            @ApiResponse(responseCode = "200", description = "Serviço técnico reagendado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> reschedule(UUID id, ServiceScheduleDTO dto);

    @Operation(
        summary = "Registra o aceite de um serviço técnico",
        description = "Transição OPEN→IN_PROGRESS para contratante autorizada ou executor ativo. "
                + "O ator vem do JWT; acceptedBy legado é opcional e ignorado. "
                + "A contratante mantém a exigência de ao menos um executor cadastrado.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false,
                description = "Corpo opcional. acceptedBy não determina o ator nem a autorização.")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aceite registrado; item completo atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida"),
            @ApiResponse(responseCode = "403", description = "Participante sem autorização para agir"),
            @ApiResponse(responseCode = "404", description = "Usuário não provisionado ou serviço inacessível"),
            @ApiResponse(responseCode = "409", description = "Estado invalido para executor ou contratante"),
            @ApiResponse(responseCode = "422", description = "Servico sem executor para aceite")
    })
    ResponseEntity<PersonalTechnicalServiceResponseDTO> accept(UUID id, ServiceAcceptanceDTO dto);

    @Operation(
        summary = "Conclui um serviço técnico",
        description = "Transição IN_PROGRESS→COMPLETED para contratante autorizada ou executor ativo. "
                + "A contratante autorizada prevalece quando o usuário também é executor."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço concluído; item completo atualizado"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida"),
            @ApiResponse(responseCode = "403", description = "Participante sem autorização para agir"),
            @ApiResponse(responseCode = "404", description = "Usuário não provisionado ou serviço inacessível"),
            @ApiResponse(responseCode = "409", description = "Estado invalido para executor ou contratante")
    })
    ResponseEntity<PersonalTechnicalServiceResponseDTO> complete(UUID id);

    @Operation(
        summary = "Cancela um serviço técnico",
        description = "Permitido a partir dos status OPEN ou IN_PROGRESS; CANCELED é um status final."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou recurso de outra empresa"),
            @ApiResponse(responseCode = "200", description = "Serviço técnico cancelado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> cancel(UUID id);

    @Operation(summary = "Busca um serviço técnico pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou recurso de outra empresa"),
            @ApiResponse(responseCode = "200", description = "Serviço técnico encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço técnico não encontrado")
    })
    ResponseEntity<TechnicalServiceResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista os serviços técnicos de um projeto técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Recurso ou parent inexistente"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou recurso de outra empresa"),
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<TechnicalServiceResponseDTO>> findByTechnicalProject(UUID technicalProjectId);

    @Operation(summary = "Lista os serviços técnicos de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Recurso ou parent inexistente"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou recurso de outra empresa"),
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<TechnicalServiceResponseDTO>> findAllByCompany(UUID companyId);
}
