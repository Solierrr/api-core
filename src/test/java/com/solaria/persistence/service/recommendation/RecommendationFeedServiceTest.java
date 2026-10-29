package com.solaria.persistence.service.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.integration.recommendation.RecommendationClient;
import com.solaria.persistence.integration.recommendation.RecommendationClientProperties;
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RecommendationFeedServiceTest {

    private static final UUID COMPANY = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    private static final UUID UNIT = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    private static final UUID OTHER_UNIT = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
    private static final UUID PROFESSION = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");

    @Mock
    private RecommendationClient client;
    @Mock
    private RecommendationFallbackService fallback;
    @Mock
    private RbacAuthorizationService rbac;
    @Mock
    private LocalUnitRepository localUnits;

    private final JsonNode body = JsonMapper.builder().build().createObjectNode();
    private MutableClock clock;
    private RecommendationFeedService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-29T12:00:00Z"));
        service = serviceWithTtl(Duration.ofSeconds(60));
    }

    private RecommendationFeedService serviceWithTtl(Duration ttl) {
        return new RecommendationFeedService(client, fallback, rbac, localUnits,
                new RecommendationClientProperties("http://rec", "k", Duration.ofSeconds(5), Duration.ofSeconds(25), ttl, 10),
                clock);
    }

    private LocalUnit unit(UUID id) {
        LocalUnit unit = new LocalUnit();
        unit.setId(id);
        return unit;
    }

    private Map<String, String> expected(String... pairs) {
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            params.put(pairs[i], pairs[i + 1]);
        }
        return params;
    }

    @Test
    void professionalsCarryTheCompanyOfTheAuthenticatedUser() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.of(COMPANY));
        when(client.feed(eq("professionals"), any())).thenReturn(body);

        assertSame(body, service.professionals("top_rated", PROFESSION));

        verify(client).feed("professionals", expected("strategy", "top_rated", "profession_id", PROFESSION.toString(),
                "company_id", COMPANY.toString()));
        verifyNoInteractions(localUnits);
    }

    @Test
    void usersWithoutCompanySendNoCompanyContext() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.empty());
        when(client.feed(eq("professionals"), any())).thenReturn(body);

        service.professionals(null, null);

        verify(client).feed("professionals", Map.of());
    }

    @Test
    void offersUseTheRequestedUnitWhenItBelongsToTheCompany() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.of(COMPANY));
        when(localUnits.findByIdAndRequester_Company_Id(UNIT, COMPANY)).thenReturn(Optional.of(unit(UNIT)));
        when(client.feed(eq("offers"), any())).thenReturn(body);

        service.offers("nearest_available", UNIT);

        verify(client).feed("offers", expected("strategy", "nearest_available", "local_unit_id", UNIT.toString(),
                "company_id", COMPANY.toString()));
    }

    @Test
    void aUnitFromAnotherCompanyIsNotFoundAndNothingIsSent() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.of(COMPANY));
        when(localUnits.findByIdAndRequester_Company_Id(OTHER_UNIT, COMPANY)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.suppliers("nearest", OTHER_UNIT));

        verifyNoInteractions(client);
    }

    @Test
    void aUnitRequestedWithoutCompanyIsNotFound() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.offers(null, UNIT));

        verifyNoInteractions(client, localUnits);
    }

    @Test
    void theOnlyUnitOfTheCompanyIsDerived() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.of(COMPANY));
        when(localUnits.findByRequester_Company_Id(COMPANY)).thenReturn(List.of(unit(UNIT)));
        when(client.feed(eq("suppliers"), any())).thenReturn(body);

        service.suppliers(null, null);

        verify(client).feed("suppliers", expected("local_unit_id", UNIT.toString(), "company_id", COMPANY.toString()));
    }

    @Test
    void severalOrNoUnitsAreNotGuessed() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.of(COMPANY));
        when(localUnits.findByRequester_Company_Id(COMPANY)).thenReturn(List.of(unit(UNIT), unit(OTHER_UNIT)));
        when(client.feed(eq("offers"), any())).thenReturn(body);

        service.offers(null, null);

        verify(client).feed("offers", expected("company_id", COMPANY.toString()));
    }

    @Test
    void rejectsAMalformedStrategyBeforeCallingTheService() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.empty());

        assertThrows(InvalidFieldException.class, () -> service.professionals("top_rated&x=1", null));
        assertThrows(InvalidFieldException.class, () -> service.professionals("../admin", null));

        verifyNoInteractions(client);
    }

    @Test
    void cachesTheFeedBrieflyPerContext() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.of(COMPANY));
        when(client.feed(eq("professionals"), any())).thenReturn(body);

        service.professionals("top_rated", null);
        clock.advance(Duration.ofSeconds(30));
        service.professionals("top_rated", null);
        service.professionals("best_match", null);

        verify(client, times(2)).feed(eq("professionals"), any());

        clock.advance(Duration.ofSeconds(31));
        service.professionals("top_rated", null);

        verify(client, times(3)).feed(eq("professionals"), any());
    }

    @Test
    void doesNotShareTheCacheAcrossCompanies() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.of(COMPANY), Optional.of(OTHER_UNIT));
        when(client.feed(eq("professionals"), any())).thenReturn(body);

        service.professionals("top_rated", null);
        service.professionals("top_rated", null);

        verify(client, times(2)).feed(eq("professionals"), any());
    }

    @Test
    void aZeroTtlDisablesTheCache() {
        service = serviceWithTtl(Duration.ZERO);
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.empty());
        when(client.feed(eq("professionals"), any())).thenReturn(body);

        service.professionals(null, null);
        service.professionals(null, null);

        verify(client, times(2)).feed(eq("professionals"), any());
    }

    @Test
    void failuresAreNeverCached() {
        when(rbac.findCurrentCompanyId()).thenReturn(Optional.empty());
        when(client.feed(eq("professionals"), any())).thenThrow(new IllegalStateException("falha")).thenReturn(body);

        assertThrows(IllegalStateException.class, () -> service.professionals(null, null));
        assertEquals(body, service.professionals(null, null));
        verify(client, times(2)).feed(eq("professionals"), any());
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
