package com.solaria.persistence.controller.recommendation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.openapi.recommendation.RecommendationFeedOpenApi;
import com.solaria.persistence.service.recommendation.RecommendationFeedService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class RecommendationFeedController implements RecommendationFeedOpenApi {

    private final RecommendationFeedService feeds;

    @Override
    @GetMapping("/professionals")
    public ResponseEntity<JsonNode> professionals(@RequestParam(required = false) String strategy,
            @RequestParam(required = false) UUID professionId) {
        return ResponseEntity.ok(feeds.professionals(strategy, professionId));
    }

    @Override
    @GetMapping("/offers")
    public ResponseEntity<JsonNode> offers(@RequestParam(required = false) String strategy,
            @RequestParam(required = false) UUID localUnitId) {
        return ResponseEntity.ok(feeds.offers(strategy, localUnitId));
    }

    @Override
    @GetMapping("/suppliers")
    public ResponseEntity<JsonNode> suppliers(@RequestParam(required = false) String strategy,
            @RequestParam(required = false) UUID localUnitId) {
        return ResponseEntity.ok(feeds.suppliers(strategy, localUnitId));
    }
}
