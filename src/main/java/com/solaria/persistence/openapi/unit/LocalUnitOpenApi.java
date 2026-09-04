package com.solaria.persistence.openapi.unit;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.unit.UpdateLocalUnitAddressIdDTO;
import com.solaria.persistence.dto.request.unit.LocalUnitRequestDTO;
import com.solaria.persistence.dto.response.unit.LocalUnitResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Unidades Locais", description = "Gerenciamento das unidades locais (unidades operacionais) das empresas")
public interface LocalUnitOpenApi {

    @Operation(
        summary = "Cria uma nova unidade local",
        description = "Exige um Requester obrigatório e imutável; o endereço é opcional na criação e pode ser anexado depois."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidade local criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<LocalUnitResponseDTO> save(LocalUnitRequestDTO dto);

    @Operation(summary = "Atualiza uma unidade local existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade local atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Unidade local não encontrada")
    })
    ResponseEntity<LocalUnitResponseDTO> update(UUID id, LocalUnitRequestDTO dto);

    @Operation(
        summary = "Vincula um endereço a uma unidade local",
        description = "Permite anexar posteriormente o endereço opcional que não foi informado na criação da unidade local."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Unidade local ou endereço não encontrado")
    })
    ResponseEntity<LocalUnitResponseDTO> attachAddress(UUID id, UpdateLocalUnitAddressIdDTO dto);

    @Operation(
        summary = "Exclui uma unidade local pelo identificador",
        description = "Não é permitida a exclusão se houver UnitSpecifications, EnergyBill ou ProposalUnit vinculados à unidade local."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unidade local excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade local não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma unidade local pelo identificador e empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade local encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade local não encontrada")
    })
    ResponseEntity<LocalUnitResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista as unidades locais de um solicitante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de unidades locais retornada com sucesso")
    })
    ResponseEntity<List<LocalUnitResponseDTO>> findByRequester(UUID requesterId);

    @Operation(summary = "Lista todas as unidades locais de uma empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de unidades locais retornada com sucesso")
    })
    ResponseEntity<List<LocalUnitResponseDTO>> findAllByCompany(UUID companyId);
}
