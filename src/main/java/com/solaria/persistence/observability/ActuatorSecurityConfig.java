package com.solaria.persistence.observability;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * declara uma {@link SecurityFilterChain}
 * dedicada e prioritaria ({@code @Order(0)}) para os endpoints do Spring Boot Actuator.
 *
 * <p>
 * mantem os endpoints de observabilidade em uma chain própria, sem isso {@code /actuator/**} 
 * cairia em outras chains e as probes do Kubernetes/load balancers receberiam {@code 401}.
 * </p>
 *  
 * <p>
 * {@link #actuatorSecurityFilterChain(HttpSecurity)} -> libera{@code /actuator/health} e {@code /actuator/info}
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
