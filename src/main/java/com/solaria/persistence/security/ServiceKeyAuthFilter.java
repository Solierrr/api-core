package com.solaria.persistence.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServiceKeyAuthFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Service-Key";

    private final ServiceKeyProperties serviceKeys;

    public ServiceKeyAuthFilter(ServiceKeyProperties serviceKeys) {
        this.serviceKeys = serviceKeys;
    }

    public static String authorityOf(String service) {
        return "SERVICE_" + service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (provided != null && !provided.isBlank()) {
            byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
            for (Map.Entry<String, String> entry : serviceKeys.entrySet()) {
                String configured = entry.getValue();
                if (configured == null || configured.isBlank()) {
                    continue;
                }
                if (MessageDigest.isEqual(configured.getBytes(StandardCharsets.UTF_8), providedBytes)) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(entry.getKey(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_SERVICE"),
                                            new SimpleGrantedAuthority(authorityOf(entry.getKey())))));
                    break;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
