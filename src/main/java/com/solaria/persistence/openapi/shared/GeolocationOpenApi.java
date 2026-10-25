package com.solaria.persistence.openapi.shared;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.shared.GeolocationRequestDTO;
import com.solaria.persistence.dto.response.shared.GeolocationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Geolocations", description = "Gerenciamento das coordenadas geográficas vinculadas a endereços.")
public interface GeolocationOpenApi {

    @Operation(
        summary = "Cadastra uma nova geolocalização",
        description = "Address proprio obrigatorio; latitude entre -90 e 90 e longitude entre -180 e 180; um endereco so pode ter no maximo uma geolocalizacao."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Geolocalização criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<GeolocationResponseDTO> save(GeolocationRequestDTO dto);

    @Operation(
        summary = "Atualiza os dados de uma geolocalização existente",
        description = "Exige owner dos enderecos atual e novo; addressId obrigatorio. Latitude entre -90 e 90 e longitude entre -180 e 180."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Geolocalização atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Geolocalização não encontrada")
    })
    ResponseEntity<GeolocationResponseDTO> update(UUID id, GeolocationRequestDTO dto);

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
    ResponseEntity<GeolocationResponseDTO> findById(UUID id);

    @Operation(summary = "Lista as geolocalizações vinculadas a um endereço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso")
    })
    ResponseEntity<List<GeolocationResponseDTO>> findByAddress(UUID addressId);
}
