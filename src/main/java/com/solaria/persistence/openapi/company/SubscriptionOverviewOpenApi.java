package com.solaria.persistence.openapi.company;

import com.solaria.persistence.dto.response.company.SubscriptionOverviewDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Plano da empresa", description = "Assinatura atual da empresa fornecedora e suas cobranças")
public interface SubscriptionOverviewOpenApi {

    @Operation(summary = "Retorna a assinatura mais recente da empresa e as cobranças",
            description = "O pagamento simulado de uma cobrança usa POST /api/charges/{id}/payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assinatura e cobranças"),
            @ApiResponse(responseCode = "404", description = "A empresa não possui assinatura")
    })
    SubscriptionOverviewDTO current();
}
