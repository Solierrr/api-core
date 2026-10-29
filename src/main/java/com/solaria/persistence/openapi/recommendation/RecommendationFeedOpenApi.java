package com.solaria.persistence.openapi.recommendation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tools.jackson.databind.JsonNode;

@Tag(name = "Recommendation Feeds", description = "Feeds de recomendação do api-recommendation para o usuário autenticado; a chave do serviço nunca chega ao navegador.")
public interface RecommendationFeedOpenApi {

    @Operation(
        summary = "Feed de profissionais recomendados",
        description = "Repassa o feed do api-recommendation com a empresa do usuário autenticado, quando houver. Cache curto no servidor. Se o grafo estiver desligado, devolve o feed público de profissionais (sem context e sem ranking)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed gerado"),
            @ApiResponse(responseCode = "400", description = "Estratégia inválida"),
            @ApiResponse(responseCode = "404", description = "Profissão não encontrada"),
            @ApiResponse(responseCode = "503", description = "Recomendações desligadas ou indisponíveis")
    })
    ResponseEntity<JsonNode> professionals(
            @Parameter(description = "top_rated, most_qualified, most_experienced, most_reliable ou best_match (padrão do serviço: best_match)") String strategy,
            @Parameter(description = "Restringe a uma profissão") UUID professionId);

    @Operation(
        summary = "Feed de ofertas recomendadas",
        description = "Repassa o feed do api-recommendation com a empresa e a unidade local do usuário autenticado. A unidade informada precisa ser da empresa do usuário; sem informar, vale a única unidade da empresa, se houver só uma. Se o grafo estiver desligado, devolve o feed público de ofertas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed gerado"),
            @ApiResponse(responseCode = "400", description = "Estratégia inválida"),
            @ApiResponse(responseCode = "404", description = "Unidade local não encontrada"),
            @ApiResponse(responseCode = "422", description = "Estratégia exige unidade local com geolocalização"),
            @ApiResponse(responseCode = "503", description = "Recomendações desligadas ou indisponíveis")
    })
    ResponseEntity<JsonNode> offers(
            @Parameter(description = "best_value, most_efficient, nearest_available ou most_proven (padrão do serviço: best_value)") String strategy,
            @Parameter(description = "Unidade local da empresa do usuário, usada em nearest_available") UUID localUnitId);

    @Operation(
        summary = "Feed de fornecedores recomendados",
        description = "Repassa o feed do api-recommendation com a empresa e a unidade local do usuário autenticado. A unidade informada precisa ser da empresa do usuário; sem informar, vale a única unidade da empresa, se houver só uma. Se o grafo estiver desligado, devolve o feed público de fornecedores."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed gerado"),
            @ApiResponse(responseCode = "400", description = "Estratégia inválida"),
            @ApiResponse(responseCode = "404", description = "Unidade local não encontrada"),
            @ApiResponse(responseCode = "422", description = "Estratégia exige unidade local com geolocalização"),
            @ApiResponse(responseCode = "503", description = "Recomendações desligadas ou indisponíveis")
    })
    ResponseEntity<JsonNode> suppliers(
            @Parameter(description = "most_proven, broadest_catalog ou nearest (padrão do serviço: most_proven)") String strategy,
            @Parameter(description = "Unidade local da empresa do usuário, usada em nearest") UUID localUnitId);
}
