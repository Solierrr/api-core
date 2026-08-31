package com.solaria.persistence.openapi.execution;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.execution.UpdateFunctionDTO;
import com.solaria.persistence.dto.request.execution.ServiceExecutorRequestDTO;
import com.solaria.persistence.dto.response.execution.ServiceExecutorResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Executores de Serviço", description = "Gerenciamento dos executores responsáveis pela prestação de serviços")
public interface ServiceExecutorOpenApi {

    @Operation(
        summary = "Cria um novo executor de serviço",
        description = "Serviço, afiliação e função são obrigatórios; só pode ser criado enquanto o serviço estiver aberto ou em andamento; a mesma afiliação não pode ser executora duas vezes do mesmo serviço."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Executor de serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para criação do executor de serviço")
    })
    ResponseEntity<ServiceExecutorResponseDTO> save(ServiceExecutorRequestDTO dto);

    @Operation(
        summary = "Atualiza a função de um executor de serviço",
        description = "Só pode ser alterado enquanto o serviço estiver aberto ou em andamento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Função do executor de serviço atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização da função"),
            @ApiResponse(responseCode = "404", description = "Executor de serviço não encontrado")
    })
    ResponseEntity<ServiceExecutorResponseDTO> updateFunction(UUID id, UpdateFunctionDTO dto);

    @Operation(
        summary = "Remove um executor de serviço pelo id",
        description = "Só pode ser removido enquanto o serviço estiver aberto ou em andamento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Executor de serviço removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Executor de serviço não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Lista os executores de um serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de executores de serviço retornada com sucesso")
    })
    ResponseEntity<List<ServiceExecutorResponseDTO>> findByService(UUID serviceId);
}
