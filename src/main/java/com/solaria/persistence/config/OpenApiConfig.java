package com.solaria.persistence.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.solaria.persistence.openapi.ProblemDetailSchema;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;


@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(name = "serviceKey", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-Service-Key")
public class OpenApiConfig {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Bean
    public OpenAPI solariaOpenApi(BuildProperties build) {
        return new OpenAPI()
                .info(new Info()
                        .title("Solaria — api-core")
                        .description("Cadastro central da plataforma: empresas, profissionais, fornecedores, produtos "
                                + "ofertados, propostas, projetos, pedidos e unidades consumidoras com as suas faturas "
                                + "de energia, cada pessoa vendo e alterando só o que o papel dela na empresa permite. "
                                + "As rotas usam o JWT emitido pelo api-auth (bearerAuth); o catálogo público não "
                                + "exige autenticação, e as rotas internas usam a chave de serviço do chamador no "
                                + "cabeçalho X-Service-Key (serviceKey).")
                        .version(build.getVersion())
                        .contact(new Contact().name("Solaria").url("https://github.com/Solierrr"))
                        .license(new License().name("CC0-1.0").url("https://creativecommons.org/publicdomain/zero/1.0/")))
                .servers(List.of(
                        new Server().url("https://api-core-cqfn.onrender.com").description("QA"),
                        new Server().url("http://localhost:8080").description("Local")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    @Profile("!openapi-export")
    public OpenApiCustomizer hideInternalRoutes() {
        return openApi -> {
            openApi.getPaths().keySet().removeIf(path -> path.startsWith("/internal/"));
            Set<String> used = openApi.getPaths().values().stream()
                    .flatMap(path -> path.readOperations().stream())
                    .flatMap(operation -> Optional.ofNullable(operation.getTags()).orElse(List.of()).stream())
                    .collect(Collectors.toSet());
            if (openApi.getTags() != null) {
                openApi.getTags().removeIf(tag -> !used.contains(tag.getName()));
            }
        };
    }

    // operationId estável e único: verbo do método + recurso do controller (ProjectController.create -> createProject)
    @Bean
    public OperationCustomizer operationIds() {
        return (operation, handlerMethod) -> {
            String method = handlerMethod.getMethod().getName();
            String resource = handlerMethod.getBeanType().getSimpleName().replaceFirst("Controller$", "");
            operation.setOperationId(method.toLowerCase().contains(resource.toLowerCase()) ? method : method + resource);
            return operation;
        };
    }

    // Toda resposta 4xx/5xx documentada usa o ProblemDetail do contrato de erro; toda operação documenta o 500
    @Bean
    public OpenApiCustomizer problemDetailResponses() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            Map<String, Schema> schemas = ModelConverters.getInstance()
                    .readAll(new AnnotatedType(ProblemDetailSchema.class));
            schemas.forEach(openApi.getComponents()::addSchemas);

            Content problem = new Content().addMediaType(PROBLEM_JSON, new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/" + ProblemDetailSchema.NAME)));
            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
                operation.getResponses().putIfAbsent("500",
                        new ApiResponse().description("Erro interno inesperado"));
                operation.getResponses().forEach((code, response) -> {
                    if (code.startsWith("4") || code.startsWith("5")) {
                        response.setContent(problem);
                    }
                });
            }));
        };
    }
}
