package com.solaria.persistence.openapi.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.dto.response.catalog.ModelPhotoResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Model Photos", description = "Gerenciamento das fotos de um modelo de placa solar, armazenadas no Cloudinary.")
public interface ModelPhotoOpenApi {

    @Operation(
        summary = "Envia uma nova foto para o modelo",
        description = "A empresa criadora autorizada faz upload da imagem para o Cloudinary e associa ao modelo informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto criada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Modelo não encontrado"),
            @ApiResponse(responseCode = "403", description = "Empresa ou grant não autorizado")
    })
    ResponseEntity<ModelPhotoResponseDTO> upload(UUID modelId, MultipartFile file);

    @Operation(summary = "Remove uma foto do modelo",
        description = "Somente a empresa criadora autorizada pode remover a foto.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada"),
            @ApiResponse(responseCode = "403", description = "Empresa ou grant não autorizado")
    })
    ResponseEntity<Void> delete(UUID id);

    @Operation(summary = "Lista as fotos cadastradas para o modelo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de fotos retornada com sucesso")
    })
    ResponseEntity<List<ModelPhotoResponseDTO>> findByModel(UUID modelId);
}
