package com.solaria.persistence.openapi.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.request.shared.PhotoCropRequestDTO;
import com.solaria.persistence.dto.response.identity.UserPhotoResponseDTO;

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

    @Operation(
        summary = "Define o recorte (ponto focal) da foto",
        description = "Retângulo em pixels da imagem original. Perfil deve ser quadrado e banner 3:1. A resposta traz as variantes prontas (avatarSm, avatar, card, banner)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recorte salvo"),
            @ApiResponse(responseCode = "400", description = "Recorte inválido para o tipo ou fora da imagem"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    })
    ResponseEntity<UserPhotoResponseDTO> updateCrop(UUID userId, PhotoType type, PhotoCropRequestDTO crop);

    @Operation(summary = "Remove o recorte e volta ao enquadramento automático")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recorte removido"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    })
    ResponseEntity<UserPhotoResponseDTO> clearCrop(UUID userId, PhotoType type);
}
