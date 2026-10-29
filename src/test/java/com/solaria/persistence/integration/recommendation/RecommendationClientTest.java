package com.solaria.persistence.integration.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.RecommendationUnavailableException;
import com.solaria.persistence.exception.ResourceNotFoundException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class RecommendationClientTest {

    private static final String KEY = "chave-de-recomendacao-de-teste-com-32-caracteres";
    private static final String UNIT = "11111111-1111-4111-8111-111111111111";
    private static final String PROFESSION = "22222222-2222-4222-8222-222222222222";
    private static final String FEED_UNAVAILABLE = "{\"detail\":{\"code\":\"FEED_UNAVAILABLE\",\"message\":\"grafo indisponível\"}}";

    private final ObjectMapper json = JsonMapper.builder().build();

    private RecommendationClientProperties properties(String baseUrl, String apiKey) {
        return new RecommendationClientProperties(baseUrl, apiKey, Duration.ofSeconds(5), Duration.ofSeconds(25),
                Duration.ofSeconds(60));
    }

    private MockRestServiceServer mock(RecommendationClientProperties properties, RecommendationClient[] out) {
        RestClient.Builder builder = new RecommendationClientConfig().recommendationRestClient(properties).mutate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        out[0] = new RecommendationClient(builder.build(), properties, json);
        return server;
    }

    private Map<String, String> params(String... pairs) {
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            params.put(pairs[i], pairs[i + 1]);
        }
        return params;
    }

    @Test
    void sendsTheKeyAndTheContextToTheAuthenticatedFeed() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/offers?strategy=nearest_available&local_unit_id=" + UNIT))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Recommendation-Key", KEY))
                .andRespond(withSuccess("{\"source\":\"graph\",\"items\":[]}", MediaType.APPLICATION_JSON));

        JsonNode body = client[0].feed("offers", params("strategy", "nearest_available", "local_unit_id", UNIT));

        assertEquals("graph", body.path("source").asString());
        server.verify();
    }

    @Test
    void omitsTheKeyHeaderWhenNoKeyIsConfigured() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", ""), client);
        server.expect(requestTo("http://rec/feeds/suppliers"))
                .andExpect(headerDoesNotExist("X-Recommendation-Key"))
                .andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));

        client[0].feed("suppliers", Map.of());

        server.verify();
    }

    @Test
    void fallsBackToThePublicFeedWithoutKeyWhenTheGraphIsOff() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/professionals?strategy=top_rated&profession_id=" + PROFESSION
                + "&company_id=" + UNIT))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON)
                        .body(FEED_UNAVAILABLE));
        server.expect(requestTo("http://rec/public/feeds/professionals?profession_id=" + PROFESSION))
                .andExpect(headerDoesNotExist("X-Recommendation-Key"))
                .andRespond(withSuccess("{\"source\":\"graph\",\"items\":[{\"name\":\"Ana\"}]}",
                        MediaType.APPLICATION_JSON));

        JsonNode body = client[0].feed("professionals",
                params("strategy", "top_rated", "profession_id", PROFESSION, "company_id", UNIT));

        assertEquals("Ana", body.path("items").get(0).path("name").asString());
        server.verify();
    }

    @Test
    void failsClearlyWhenThePublicFallbackFailsToo() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON).body(FEED_UNAVAILABLE));
        server.expect(requestTo("http://rec/public/feeds/offers"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(RecommendationUnavailableException.class, () -> client[0].feed("offers", Map.of()));
        server.verify();
    }

    @Test
    void retriesOnceWhenTheServiceIsWakingUp() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withException(new IOException("timeout")));
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withSuccess("{\"items\":[]}",
                MediaType.APPLICATION_JSON));

        JsonNode body = client[0].feed("offers", Map.of());

        assertTrue(body.has("items"));
        server.verify();
    }

    @Test
    void retriesOnceOnBadGatewayAndThenGivesUp() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(RecommendationUnavailableException.class, () -> client[0].feed("offers", Map.of()));
        server.verify();
    }

    @Test
    void givesUpAfterTheRetryWhenTheServiceIsUnreachable() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withException(new IOException("timeout")));
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withException(new IOException("timeout")));

        assertThrows(RecommendationUnavailableException.class, () -> client[0].feed("offers", Map.of()));
        server.verify();
    }

    @Test
    void doesNotRetryOtherServerErrors() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(RecommendationUnavailableException.class, () -> client[0].feed("offers", Map.of()));
        server.verify();
    }

    @Test
    void mapsContextErrorsFromTheFeed() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/professionals")).andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"detail\":{\"code\":\"PROFESSION_NOT_FOUND\",\"message\":\"Profissão não encontrada.\"}}"));
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"detail\":{\"code\":\"LOCAL_UNIT_REQUIRED\",\"message\":\"Informe a unidade.\"}}"));
        server.expect(requestTo("http://rec/feeds/suppliers")).andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .contentType(MediaType.APPLICATION_JSON).body("{\"detail\":[{\"msg\":\"valor inválido\"}]}"));

        ResourceNotFoundException notFound = assertThrows(ResourceNotFoundException.class,
                () -> client[0].feed("professionals", Map.of()));
        BusinessRuleException unit = assertThrows(BusinessRuleException.class,
                () -> client[0].feed("offers", Map.of()));
        BusinessRuleException generic = assertThrows(BusinessRuleException.class,
                () -> client[0].feed("suppliers", Map.of()));

        assertEquals("Profissão não encontrada.", notFound.getMessage());
        assertEquals("Informe a unidade.", unit.getMessage());
        assertFalse(generic.getMessage().isBlank());
    }

    @Test
    void aRejectedKeyBecomesUnavailabilityWithoutLeakingIt() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("http://rec", KEY), client);
        server.expect(requestTo("http://rec/feeds/offers")).andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON).body("{\"detail\":\"Chave de recomendação inválida\"}"));

        RecommendationUnavailableException error = assertThrows(RecommendationUnavailableException.class,
                () -> client[0].feed("offers", Map.of()));

        assertFalse(error.getMessage().contains(KEY));
        server.verify();
    }

    @Test
    void isDisabledWithoutBaseUrlAndNeverCallsOut() {
        RecommendationClient[] client = new RecommendationClient[1];
        MockRestServiceServer server = mock(properties("", KEY), client);

        assertThrows(RecommendationUnavailableException.class, () -> client[0].feed("offers", Map.of()));
        server.verify();
    }
}
