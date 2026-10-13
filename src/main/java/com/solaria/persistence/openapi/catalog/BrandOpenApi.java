package com.solaria.persistence.openapi.catalog;

import com.solaria.persistence.dto.request.catalog.BrandRequestDTO;
import com.solaria.persistence.dto.response.catalog.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Tag(
        name = "Marcas",
        description =
                "Catálogo público e privado de marcas; escrita sujeita ao RBAC existente e à"
                    + " autoria empresarial")
public interface BrandOpenApi {
    @Operation(summary = "Lista marcas públicas e marcas da empresa autenticada")
    List<BrandResponseDTO> list();

    @Operation(
            summary = "Cria marca privada",
            description = "Publicação exige solicitação e aprovação humana do Admin Solaria")
    BrandResponseDTO create(BrandRequestDTO dto);

    @Operation(
            summary = "Edita marca privada da empresa",
            description =
                    "Solicitação pendente bloqueia edição; marca pública exige solicitação de"
                        + " correção")
    BrandResponseDTO update(UUID id, BrandRequestDTO dto);

    @Operation(
            summary = "Arquiva marca privada",
            description =
                    "Preserva vínculos existentes e impede novos vínculos; marcas públicas não"
                        + " podem ser arquivadas")
    void archive(UUID id);

    @Operation(
            summary = "Solicita publicação ou correção",
            description =
                    "Snapshot imutável enquanto pendente; dados aprovados permanecem vigentes até a"
                        + " decisão")
    BrandRequestResponseDTO submit(UUID id, BrandRequestDTO dto);

    @Operation(summary = "Envia logo opcional", description = "JPEG, PNG ou WebP, até 8 MB")
    Map<String, String> logo(MultipartFile file);
}
