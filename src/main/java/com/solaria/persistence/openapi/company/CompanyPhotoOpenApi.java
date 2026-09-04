package com.solaria.persistence.openapi.company;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.domain.enums.shared.PhotoType;
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
}
