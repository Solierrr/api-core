package com.solaria.persistence.observability;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Declara uma {@link SecurityFilterChain} dedicada e prioritária ({@code @Order(0)}) para o Actuator
 *
 * <p>
 * Sem chain própria, {@code /actuator/**} cairia nas chains da API
 * -> probes do Kubernetes e load balancers receberiam {@code 401}
 * </p>
 *
 * <p>
 * {@link #actuatorSecurityFilterChain(HttpSecurity)} -> libera {@code /actuator/health} e {@code /actuator/info},
 * nega o resto
 * </p>
 */
@Configuration
public class ActuatorSecurityConfig {

    /**
     * Chain de seguranca aplicada apenas aos endpoints do Actuator
     * {@code @Order(0)} -> prioridade maior de chain
     *
     * @return chain configurada para {@link EndpointRequest#toAnyEndpoint()}
     */
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // restringe esta chain apenas aos endpoints do Actuator
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                // health/info liberados para probes e checagens externas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        .anyRequest().denyAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
