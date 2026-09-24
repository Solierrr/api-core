package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.company.UpdateBusinessContactIdDTO;
import com.solaria.persistence.dto.patch.company.UpdateCompanyAddressIdDTO;
import com.solaria.persistence.dto.request.company.CompanyRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Empresas", description = "Gerenciamento das empresas cadastradas na plataforma")
public interface CompanyOpenApi {

    @Operation(
        summary = "Cria uma nova empresa",
        description = "Endereço e contato comercial são opcionais na criação; CNPJ é obrigatório (14 dígitos), único e imutável, com dígito verificador validado por trigger. A empresa nasce no status UNDER_ANALYSIS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado para outra empresa")
    })
    ResponseEntity<CompanyResponseDTO> save(CompanyRequestDTO dto);

    @Operation(summary = "Atualiza os dados de uma empresa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<CompanyResponseDTO> update(UUID id, CompanyRequestDTO dto);

    @Operation(summary = "Vincula um endereço a uma empresa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa ou endereço não encontrado")
    })
    ResponseEntity<CompanyResponseDTO> attachAddress(UUID id, UpdateCompanyAddressIdDTO dto);

    @Operation(summary = "Vincula um contato comercial a uma empresa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contato comercial vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa ou contato comercial não encontrado")
    })
    ResponseEntity<CompanyResponseDTO> attachBusinessContact(UUID id, UpdateBusinessContactIdDTO dto);

    @Operation(
        summary = "Aprova o cadastro de uma empresa",
        description = "Só é permitido a partir do status UNDER_ANALYSIS; a aprovação é definitiva."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa aprovada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<CompanyResponseDTO> approve(UUID id);

    @Operation(
        summary = "Rejeita o cadastro de uma empresa",
        description = "Só é permitido a partir do status UNDER_ANALYSIS; a rejeição é definitiva."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa rejeitada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<CompanyResponseDTO> reject(UUID id);

    @Operation(
        summary = "Remove uma empresa",
        description = "A exclusão é bloqueada se houver qualquer vínculo (usuário, fornecedor, solicitante, técnico afiliado, cargo)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empresa removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca uma empresa pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<CompanyResponseDTO> findById(UUID id);

    @Operation(summary = "Busca uma empresa pelo CNPJ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<CompanyResponseDTO> findByCnpj(String cnpj);

    @Operation(summary = "Lista todas as empresas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<CompanyResponseDTO>> findAll();
}
