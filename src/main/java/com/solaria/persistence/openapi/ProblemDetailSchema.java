package com.solaria.persistence.openapi;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

// Só documenta o corpo de erro (application/problem+json); quem monta o corpo é o ProblemDetailFactory
@Schema(name = ProblemDetailSchema.NAME, description = "Corpo de todo erro HTTP, no contrato de erro da organização")
public record ProblemDetailSchema(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Sempre about:blank",
                example = "about:blank") String type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "O mesmo valor de errorCode",
                example = "NOT_FOUND") String title,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Código HTTP",
                example = "404") int status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Código estável do catálogo de erros, "
                + "usado pelo front para traduzir a mensagem", example = "NOT_FOUND") String errorCode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Explicação para quem depura; o front "
                + "não exibe", example = "Projeto não encontrado") String detail,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Caminho da requisição",
                example = "/api/projects/3f2b8c1e-5d4a-4e9b-9c7d-1a2b3c4d5e6f") String instance,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Momento do erro, em UTC",
                example = "2026-10-24T13:45:00Z") Instant timestamp,
        @Schema(description = "Id do trace, para cruzar com os logs; presente quando há trace",
                example = "4bf92f3577b34da6a3ce929d0e0e4736") String traceId,
        @Schema(description = "Erros por campo; presente só em VALIDATION_FAILED") List<FieldError> errors) {

    public static final String NAME = "ProblemDetail";

    @Schema(name = "ProblemFieldError", description = "Erro de validação de um campo ou parâmetro")
    public record FieldError(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Campo ou parâmetro inválido",
                    example = "name") String field,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Código do erro do campo",
                    example = "VALIDATION_FAILED") String errorCode,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Explicação para quem depura",
                    example = "name é obrigatório") String detail) {
    }
}
