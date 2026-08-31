package com.solaria.persistence.openapi.shared;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.shared.GeolocalizationRequestDTO;
import com.solaria.persistence.dto.response.shared.GeolocalizationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Geolocalizations", description = "Gerenciamento das coordenadas geográficas vinculadas a endereços.")
public interface GeolocalizationOpenApi {

    @Operation(
        summary = "Cadastra uma nova geolocalização",
        description = "Latitude deve estar entre -90 e 90 e longitude entre -180 e 180; um endereço só pode ter no máximo uma geolocalização."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Geolocalização criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<GeolocalizationResponseDTO> save(GeolocalizationRequestDTO dto);

    @Operation(
        summary = "Atualiza os dados de uma geolocalização existente",
        description = "Latitude deve estar entre -90 e 90 e longitude entre -180 e 180."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Geolocalização atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Geolocalização não encontrada")
    })
    ResponseEntity<GeolocalizationResponseDTO> update(UUID id, GeolocalizationRequestDTO dto);

    @Operation(summary = "Remove uma geolocalização pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Geolocalização removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Geolocalização não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma geolocalização pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Geolocalização encontrada"),
            @ApiResponse(responseCode = "404", description = "Geolocalização não encontrada")
    })
    ResponseEntity<GeolocalizationResponseDTO> findById(UUID id);

    @Operation(summary = "Lista as geolocalizações vinculadas a um endereço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<GeolocalizationResponseDTO>> findByAddress(UUID addressId);
}
