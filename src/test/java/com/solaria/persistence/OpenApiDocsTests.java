package com.solaria.persistence;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void servedContractIsPublicAndHidesInternalRoutes() throws Exception {
        mockMvc.perform(get("/openapi.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Solaria — api-core"))
                .andExpect(jsonPath("$.info.version").value(matchesPattern("\\d+\\.\\d+\\.\\d+")))
                .andExpect(jsonPath("$.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.components.securitySchemes.serviceKey.name").value("X-Service-Key"))
                .andExpect(jsonPath("$.components.schemas.ProblemDetail").exists())
                .andExpect(jsonPath("$.paths['/api/catalog/offers']").exists())
                .andExpect(jsonPath("$.paths['/internal/users']").doesNotExist());
    }

    @Test
    void docsRedirectsToSwaggerUi() throws Exception {
        mockMvc.perform(get("/docs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("/swagger-ui/index.html")));
    }
}
