package com.solaria.persistence.openapi;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.domain.enums.PhotoType;
import com.solaria.persistence.dto.response.UserPhotoResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User Photos", description = "Gerenciamento das fotos (perfil/banner) de um usuário, armazenadas no Cloudinary.")
public interface UserPhotoOpenApi {

    @Operation(
        summary = "Envia (ou substitui) a foto de um tipo para o usuário",
        description = "Faz upload da imagem para o Cloudinary e associa ao usuário e ao tipo informado (PROFILE/BANNER)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto enviada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    ResponseEntity<UserPhotoResponseDTO> upload(UUID userId, PhotoType type, MultipartFile file);

    @Operation(summary = "Remove a foto de um tipo do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    })
    ResponseEntity<Void> delete(UUID userId, PhotoType type);

    @Operation(summary = "Lista as fotos cadastradas para o usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de fotos retornada com sucesso")
    })
    ResponseEntity<List<UserPhotoResponseDTO>> findByUser(UUID userId);
}
