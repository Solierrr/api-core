package com.solaria.persistence.openapi.execution;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.execution.ServiceContractRequestDTO;
import com.solaria.persistence.dto.response.execution.ServiceContractResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "service Contracts", description = "Gerenciamento dos contratos de prestação de serviço, imutáveis após criação.")
public interface ServiceContractOpenApi {

    @Operation(
        summary = "Cria um novo contrato de serviço",
        description = "O prazo de entrega, se informado, não pode estar no passado; seguro é obrigatório; só pode ser criado se o serviço estiver IN_PROGRESS; apenas um contrato por serviço."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrato de serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Já existe um contrato de serviço para o serviço informado")
    })
    ResponseEntity<ServiceContractResponseDTO> save(ServiceContractRequestDTO dto);

    @Operation(
        summary = "Atualiza os dados de um contrato de serviço existente",
        description = "O prazo de entrega, se informado, não pode estar no passado; seguro é obrigatório."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrato de serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Contrato de serviço não encontrado")
    })
    ResponseEntity<ServiceContractResponseDTO> update(UUID id, ServiceContractRequestDTO dto);

    @Operation(
        summary = "Marca o contrato como aprovado pela concessionária de energia",
        description = "A aprovação da concessionária nasce como não aprovada e só pode ser marcada como aprovada; não há transição de volta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aprovação da concessionária registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Contrato de serviço não encontrado"),
            @ApiResponse(responseCode = "409", description = "Contrato em estado incompatível com esta transição")
    })
    ResponseEntity<ServiceContractResponseDTO> markUtilityApproved(UUID id);

    @Operation(summary = "Busca um contrato de serviço pelo identificador, escopado à empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrato de serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Contrato de serviço não encontrado")
    })
    ResponseEntity<ServiceContractResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Busca o contrato de serviço vinculado a um serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrato de serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Contrato de serviço não encontrado")
    })
    ResponseEntity<ServiceContractResponseDTO> findByService(UUID serviceId);
}
