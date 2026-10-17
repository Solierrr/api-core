package com.solaria.persistence.openapi.catalog;

import com.solaria.persistence.dto.request.catalog.BrandDecisionDTO;
import com.solaria.persistence.dto.response.catalog.BrandRequestResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@Tag(name = "Solicitações de marcas")
public interface BrandRequestOpenApi {
    @Operation(summary = "Lista somente solicitações da empresa autenticada")
    List<BrandRequestResponseDTO> own();

    @Operation(
            summary = "Lista solicitações para análise",
            description = "Exclusivo do Admin Solaria")
    List<BrandRequestResponseDTO> review();

    @Operation(
            summary = "Cancela solicitação pendente da empresa",
            description = "Exige revisão atual e desbloqueia edição")
    BrandRequestResponseDTO cancel(UUID id, BrandDecisionDTO dto);

    @Operation(
            summary = "Aprova ou reprova solicitação",
            description =
                    "Exclusivo do Admin Solaria; motivo obrigatório na reprovação; revisão"
                        + " divergente não é aceita")
    BrandRequestResponseDTO decide(UUID id, boolean approved, BrandDecisionDTO dto);
}
