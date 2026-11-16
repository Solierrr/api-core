package com.solaria.persistence.security;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import com.solaria.persistence.service.professional.ProfessionalOnboardingService;

@Component
public class MobileSessionGuard {
    private static final Set<String> ALLOWED = Set.of(
            "GET /api/users/me", "GET /api/users/me/mobile-eligibility",
            "GET /api/connections", "GET /api/connections/{id}",
            "GET /api/connections/people", "GET /api/connections/people/{id}",
            "POST /api/connections", "DELETE /api/connections/{id}", "GET /api/professions");
    private final ProfessionalOnboardingService onboarding;
    private final RestClient auth;
    private final String serviceToken;
    private final String clientId;

    public MobileSessionGuard(ProfessionalOnboardingService onboarding,
            @Value("${app.mobile.auth-base-url:http://localhost:8081}") String baseUrl,
            @Value("${app.mobile.service-token:}") String serviceToken,
            @Value("${app.mobile.client-id:solaria-mobile}") String clientId) {
        this.onboarding = onboarding;
        this.serviceToken = serviceToken;
        this.clientId = clientId;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        auth = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public void requireAccess(String endpoint) {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken authentication)) return;
        var jwt = authentication.getToken();
        var tokenClient = jwt.getClaimAsString("client_id");
        var context = jwt.getClaimAsString("context");
        if (!clientId.equals(tokenClient) && !"professional".equals(context)) return;
        if (!clientId.equals(tokenClient) || !"professional".equals(context)
                || (!ALLOWED.contains(endpoint) && !endpoint.contains(" /api/projects") && !endpoint.startsWith("GET /api/catalog/"))) deny();
        UUID authId;
        UUID sessionId;
        try { authId = UUID.fromString(jwt.getSubject()); sessionId = UUID.fromString(jwt.getClaimAsString("sid")); }
        catch (Exception exception) { deny(); return; }
        if (!onboarding.eligibility(authId).eligible()) deny();
        if (serviceToken.length() < 32) unavailable();
        ActiveSession session;
        try {
            session = auth.get().uri("/internal/sessions/{id}", sessionId)
                    .header("Authorization", "Bearer " + serviceToken).retrieve().body(ActiveSession.class);
        } catch (Exception exception) { unavailable(); return; }
        if (session == null || !session.active() || !authId.equals(session.authId())
                || !clientId.equals(session.clientId()) || !"professional".equals(session.context())) deny();
    }

    private void deny() { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mobile professional access denied"); }
    private void unavailable() { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Session verification unavailable"); }
    public record ActiveSession(boolean active, UUID authId, String clientId, String context) {}
}
