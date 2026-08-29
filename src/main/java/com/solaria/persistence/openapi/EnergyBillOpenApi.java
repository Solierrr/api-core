package com.solaria.persistence.openapi;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.dto.request.EnergyBillRequestDTO;
import com.solaria.persistence.dto.response.EnergyBillResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Contas de Energia", description = "Registro e consulta de contas de energia")
public interface EnergyBillOpenApi {

    @Operation(
        summary = "Registra uma nova conta de energia",
        description = "Consumo é obrigatório e deve ser maior que zero; preço é obrigatório e deve ser maior ou igual a zero."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta de energia criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    ResponseEntity<EnergyBillResponseDTO> save(EnergyBillRequestDTO dto);

    @Operation(
        summary = "Atualiza os dados de uma conta de energia existente",
        description = "Consumo é obrigatório e deve ser maior que zero; preço é obrigatório e deve ser maior ou igual a zero."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta de energia atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Conta de energia não encontrada")
    })
    ResponseEntity<EnergyBillResponseDTO> update(UUID id, EnergyBillRequestDTO dto);

    @Operation(summary = "Busca uma conta de energia pelo id e pela empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta de energia encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta de energia não encontrada")
    })
    ResponseEntity<EnergyBillResponseDTO> findById(UUID id, UUID companyId);

    @Operation(summary = "Lista as contas de energia de uma unidade local")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    })
    ResponseEntity<List<EnergyBillResponseDTO>> findByLocalUnit(UUID localUnitId);

    @Operation(
        summary = "Anexa (ou substitui) a foto da conta de energia",
        description = "Faz upload da imagem para o Cloudinary e associa à conta de energia informada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto anexada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta de energia não encontrada")
    })
    ResponseEntity<EnergyBillResponseDTO> attachPhoto(UUID id, MultipartFile file);

    @Operation(summary = "Remove a foto da conta de energia")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta de energia não encontrada")
    })
    ResponseEntity<Void> removePhoto(UUID id);
}
