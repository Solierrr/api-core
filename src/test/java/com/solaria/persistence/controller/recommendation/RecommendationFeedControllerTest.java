package com.solaria.persistence.controller.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.solaria.persistence.service.recommendation.RecommendationFeedService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RecommendationFeedControllerTest {

    @Mock
    private RecommendationFeedService feeds;

    private RecommendationFeedController controller;
    private final JsonNode body = JsonMapper.builder().build().createObjectNode();

    @BeforeEach
    void setUp() {
        controller = new RecommendationFeedController(feeds);
    }

    @Test
    void shouldReturnTheProfessionalsFeed() {
        UUID professionId = UUID.randomUUID();
        when(feeds.professionals("top_rated", professionId)).thenReturn(body);

        ResponseEntity<JsonNode> result = controller.professionals("top_rated", professionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(body, result.getBody());
    }

    @Test
    void shouldReturnTheOffersFeed() {
        UUID unitId = UUID.randomUUID();
        when(feeds.offers("best_value", unitId)).thenReturn(body);

        ResponseEntity<JsonNode> result = controller.offers("best_value", unitId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(body, result.getBody());
    }

    @Test
    void shouldReturnTheSuppliersFeed() {
        when(feeds.suppliers(null, null)).thenReturn(body);

        ResponseEntity<JsonNode> result = controller.suppliers(null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(body, result.getBody());
    }
}
