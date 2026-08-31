package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.company.SubscriptionRequestDTO;
import com.solaria.persistence.dto.response.company.SubscriptionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Assinaturas", description = "Gerenciamento de assinaturas de planos/serviços contratados pelas empresas")
public interface SubscriptionOpenApi {

    @Operation(
        summary = "Cria uma nova assinatura",
        description = "Nasce sempre com status PAID, início definido como agora e sem data de fim; só é permitida uma assinatura aberta por fornecedor por vez."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assinatura criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<SubscriptionResponseDTO> save(SubscriptionRequestDTO dto);

    @Operation(
        summary = "Marca uma assinatura como inadimplente",
        description = "Transição PAID→IN_DEBT; não é permitida caso a assinatura já tenha sido encerrada (data de fim definida)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assinatura marcada como inadimplente com sucesso"),
            @ApiResponse(responseCode = "404", description = "Assinatura não encontrada"),
            @ApiResponse(responseCode = "409", description = "Assinatura em estado que não permite essa transição")
    })
    ResponseEntity<SubscriptionResponseDTO> markInDebt(UUID id);

    @Operation(
        summary = "Suspende uma assinatura",
        description = "Transição IN_DEBT→SUSPENDED; não é permitida caso a assinatura já tenha sido encerrada (data de fim definida)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assinatura suspensa com sucesso"),
            @ApiResponse(responseCode = "404", description = "Assinatura não encontrada"),
            @ApiResponse(responseCode = "409", description = "Assinatura em estado que não permite suspensão")
    })
    ResponseEntity<SubscriptionResponseDTO> suspend(UUID id);

    @Operation(
        summary = "Reativa uma assinatura suspensa",
        description = "Transição de volta para PAID; não é permitida caso a assinatura já tenha sido encerrada (data de fim definida)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assinatura reativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Assinatura não encontrada"),
            @ApiResponse(responseCode = "409", description = "Assinatura em estado que não permite reativação")
    })
    ResponseEntity<SubscriptionResponseDTO> reactivate(UUID id);

    @Operation(
        summary = "Encerra uma assinatura",
        description = "Define a data de fim; uma vez encerrada, nenhuma nova transição de estado é permitida."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assinatura encerrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Assinatura não encontrada"),
            @ApiResponse(responseCode = "409", description = "Assinatura em estado que não permite encerramento")
    })
    ResponseEntity<SubscriptionResponseDTO> end(UUID id);

    @Operation(summary = "Busca uma assinatura pelo identificador e empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assinatura encontrada"),
            @ApiResponse(responseCode = "404", description = "Assinatura não encontrada")
    })
    ResponseEntity<SubscriptionResponseDTO> findById(UUID id, UUID companyId);


    @Operation(summary = "Lista as assinaturas de um fornecedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de assinaturas retornada com sucesso")
    })
    ResponseEntity<List<SubscriptionResponseDTO>> findBySupplier(UUID supplierId);

    @Operation(summary = "Lista todas as assinaturas de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de assinaturas retornada com sucesso")
    })
    ResponseEntity<List<SubscriptionResponseDTO>> findAllByCompany(UUID companyId);
}
