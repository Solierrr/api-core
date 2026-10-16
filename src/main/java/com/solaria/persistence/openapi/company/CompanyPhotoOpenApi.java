package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.request.shared.PhotoCropRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPhotoResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company Photos", description = "Gerenciamento das fotos (perfil/banner) de uma empresa, armazenadas no Cloudinary.")
public interface CompanyPhotoOpenApi {

    @Operation(
        summary = "Envia (ou substitui) a foto de um tipo para a empresa",
        description = "Faz upload da imagem para o Cloudinary e associa à empresa e ao tipo informado (PROFILE/BANNER)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto enviada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    ResponseEntity<CompanyPhotoResponseDTO> upload(UUID companyId, PhotoType type, MultipartFile file);

    @Operation(summary = "Remove a foto de um tipo da empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    })
    ResponseEntity<Void> delete(UUID companyId, PhotoType type);

    @Operation(summary = "Lista as fotos cadastradas para a empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de fotos retornada com sucesso")
    })
    ResponseEntity<List<CompanyPhotoResponseDTO>> findByCompany(UUID companyId);

    @Operation(
        summary = "Define o recorte (ponto focal) da foto",
        description = "Retângulo em pixels da imagem original. Perfil deve ser quadrado e banner 3:1. A resposta traz as variantes prontas (avatarSm, avatar, card, banner)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recorte salvo"),
            @ApiResponse(responseCode = "400", description = "Recorte inválido para o tipo ou fora da imagem"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    })
    ResponseEntity<CompanyPhotoResponseDTO> updateCrop(UUID companyId, PhotoType type, PhotoCropRequestDTO crop);

    @Operation(summary = "Remove o recorte e volta ao enquadramento automático")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recorte removido"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    })
    ResponseEntity<CompanyPhotoResponseDTO> clearCrop(UUID companyId, PhotoType type);
}
