package com.solaria.persistence.service.execution;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.solaria.persistence.config.GoogleTranslateProperties;
import com.solaria.persistence.domain.entity.execution.ProjectLocation;
import com.solaria.persistence.dto.response.execution.ProjectResponse;
import com.solaria.persistence.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectGeocodingService {
    private final ProjectService projects;
    private final ProjectAccess access;
    private final RestClient googleTranslateRestClient;
    private final GoogleTranslateProperties properties;

    public record Result(Double latitude, Double longitude, @JsonProperty("place_id") String placeId,
            @JsonProperty("partial_match") boolean partialMatch) {}
    public record Results(List<Result> results) {}

    @Transactional
    public ProjectResponse resolve(UUID id) {
        var project = projects.entity(id, true);
        access.read(project);
        var location = project.getLocation();
        if (location == null || !location.isUsable()) throw new BusinessRuleException("Complete o endereço do projeto");
        if (fresh(location)) return projects.response(project);
        if (properties.getConsumerToken() == null || properties.getConsumerToken().isBlank())
            throw new BusinessRuleException("Localização temporariamente indisponível");
        var address = java.util.Arrays.asList(location.getStreet(), location.getNumber(), location.getNeighborhood(),
                location.getCity(), location.getState(), location.getZipCode(), "Brasil").stream()
                .filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(", "));
        try {
            var response = googleTranslateRestClient.post().uri("/v1/address/geocode")
                    .headers(h -> h.setBearerAuth(properties.getConsumerToken()))
                    .body(Map.of("address", address, "language", "pt-BR")).retrieve().body(Results.class);
            if (response == null || response.results() == null || response.results().isEmpty())
                throw new BusinessRuleException("Não encontramos esse endereço no mapa");
            var point = response.results().getFirst();
            if (point.partialMatch() || !valid(point.latitude(), point.longitude()))
                throw new BusinessRuleException("Revise o endereço para localizar o projeto com precisão");
            location.setLatitude(point.latitude());
            location.setLongitude(point.longitude());
            location.setPlaceId(point.placeId());
            location.setGeocodedAt(OffsetDateTime.now());
            return projects.response(project);
        } catch (org.springframework.web.client.RestClientException exception) {
            throw new BusinessRuleException("Não foi possível localizar o endereço agora. Tente novamente");
        }
    }

    public static boolean valid(Double latitude, Double longitude) {
        return latitude != null && longitude != null && Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    public static boolean fresh(ProjectLocation location) {
        return valid(location.getLatitude(), location.getLongitude()) && (location.getGeocodedAt() == null
                || location.getGeocodedAt().isAfter(OffsetDateTime.now().minusDays(29)));
    }
}
