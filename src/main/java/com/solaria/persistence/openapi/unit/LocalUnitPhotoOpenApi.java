package com.solaria.persistence.openapi.unit;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.dto.response.unit.LocalUnitPhotoResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Local Unit Photos", description = "Gerenciamento das fotos de uma unidade local, armazenadas no Cloudinary.")
public interface LocalUnitPhotoOpenApi {

    @Operation(
        summary = "Envia uma nova foto para a unidade local",
        description = "Faz upload da imagem para o Cloudinary e associa à unidade local informada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto criada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade local não encontrada")
    })
    ResponseEntity<LocalUnitPhotoResponseDTO> upload(UUID localUnitId, MultipartFile file);

    @Operation(summary = "Remove uma foto da unidade local")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    })
    ResponseEntity<Void> delete(UUID id);

    @Operation(summary = "Lista as fotos cadastradas para a unidade local")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de fotos retornada com sucesso")
    })
    ResponseEntity<List<LocalUnitPhotoResponseDTO>> findByLocalUnit(UUID localUnitId);
}
