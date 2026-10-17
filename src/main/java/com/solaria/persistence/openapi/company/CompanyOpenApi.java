package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.patch.company.UpdateCompanyAddressIdDTO;
import com.solaria.persistence.dto.request.company.CompanyCreateRequestDTO;
import com.solaria.persistence.dto.request.company.CompanyUpdateRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Empresas", description = "Gerenciamento das empresas cadastradas na plataforma")
public interface CompanyOpenApi {

    @Operation(
        summary = "Cria uma nova empresa",
        description = "Endereço e contato comercial são opcionais na criação; CNPJ é obrigatório (14 dígitos), único e imutável, com dígito verificador validado por trigger. A empresa nasce APPROVED com ADMIN privado e vínculo do criador na mesma transação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado para outra empresa")
    })
    ResponseEntity<CompanyResponseDTO> save(CompanyCreateRequestDTO dto);

    @Operation(summary = "Atualiza os dados de uma empresa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<CompanyResponseDTO> update(UUID id, CompanyUpdateRequestDTO dto);

    @Operation(summary = "Vincula um endereço a uma empresa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa ou endereço não encontrado")
    })
    ResponseEntity<CompanyResponseDTO> attachAddress(UUID id, UpdateCompanyAddressIdDTO dto);

    @Operation(
        summary = "Remove uma empresa",
        description = "A exclusão é bloqueada se houver qualquer vínculo (usuário, fornecedor, solicitante, técnico afiliado, cargo)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empresa removida com sucesso"),
            @ApiResponse(responseCode = "403", description = "Grant insuficiente ou Company de outro ator"),
            @ApiResponse(responseCode = "409", description = "Empresa possui referencias"),
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
