package com.solaria.persistence.openapi.company;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.dto.request.company.ChargeRequestDTO;
import com.solaria.persistence.dto.response.company.ChargeResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cobranças", description = "Gerenciamento de cobranças financeiras e seu ciclo de vida (pagamento, cancelamento, estorno)")
public interface ChargeOpenApi {

    @Operation(
        summary = "Cria uma nova cobrança",
        description = "Valor deve ser maior que zero e vencimento não pode estar no passado (checado só na criação manual); a cobrança nasce PENDING."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cobrança criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<ChargeResponseDTO> save(ChargeRequestDTO dto);

    @Operation(
        summary = "Registra o pagamento de uma cobrança",
        description = "Só é permitido a partir do estado PENDING; o ciclo PENDING→PAID é final para essa transição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cobrança marcada como paga com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada"),
            @ApiResponse(responseCode = "409", description = "Cobrança em estado que não permite pagamento")
    })
    ResponseEntity<ChargeResponseDTO> pay(UUID id);

    @Operation(
        summary = "Cancela uma cobrança",
        description = "Só é permitido a partir do estado PENDING; o ciclo PENDING→CANCELED é final."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cobrança cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada"),
            @ApiResponse(responseCode = "409", description = "Cobrança em estado que não permite cancelamento")
    })
    ResponseEntity<ChargeResponseDTO> cancel(UUID id);

    @Operation(
        summary = "Estorna uma cobrança",
        description = "Só é permitido a partir do estado PAID; o ciclo PAID→REFUNDED é final."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cobrança estornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada"),
            @ApiResponse(responseCode = "409", description = "Cobrança em estado que não permite estorno")
    })
    ResponseEntity<ChargeResponseDTO> refund(UUID id);

    @Operation(
        summary = "Gera uma cobrança para uma assinatura chamando a procedure sp_generate_subscription_charge",
        description = "O valor da cobrança vem do plano da assinatura, não é informado pelo cliente; execução delegada à procedure sp_generate_subscription_charge (schema-v3-business-logic.sql, Bloco C)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cobrança gerada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Assinatura não encontrada")
    })
    ResponseEntity<Void> generateFromSubscription(UUID subscriptionId, LocalDate dueDate, PaymentMethod paymentMethod);

    @Operation(summary = "Busca uma cobrança pelo identificador e empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cobrança encontrada"),
            @ApiResponse(responseCode = "404", description = "Cobrança não encontrada")
    })
    ResponseEntity<ChargeResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista as cobranças de uma assinatura")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cobranças retornada com sucesso")
    })
    ResponseEntity<List<ChargeResponseDTO>> findBySubscription(UUID subscriptionId);

    @Operation(summary = "Lista todas as cobranças de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cobranças retornada com sucesso")
    })
    ResponseEntity<List<ChargeResponseDTO>> findAllByCompany(UUID companyId);
}
