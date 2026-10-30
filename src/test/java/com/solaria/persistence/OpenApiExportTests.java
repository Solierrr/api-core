package com.solaria.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// Gera o openapi.yaml versionado (make openapi); o perfil openapi-export mantém as rotas /internal/
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("openapi-export")
class OpenApiExportTests {

    private static final Path CONTRACT = Path.of("openapi.yaml");

    @Autowired
    MockMvc mockMvc;

    @Test
    void exportsTheContractWithInternalRoutes() throws Exception {
        String yaml = mockMvc.perform(get("/openapi.json.yaml"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        assertThat(yaml).contains("/internal/users:");

        // o release-please atualiza a versão por esta marca, sem reformatar o arquivo
        String contract = yaml.replaceFirst("(?m)^(  version: \\d+\\.\\d+\\.\\d+)$", "$1 # x-release-please-version");
        assertThat(contract).contains("# x-release-please-version");
        Files.writeString(CONTRACT, contract, StandardCharsets.UTF_8);
    }
}
