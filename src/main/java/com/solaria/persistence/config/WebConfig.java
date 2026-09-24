package com.solaria.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.solaria.persistence.security.rbac.EndpointAuthorizationInterceptor;

/**
 * Configuração MVC genérica
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EndpointAuthorizationInterceptor endpointAuthorizationInterceptor;

    public WebConfig(EndpointAuthorizationInterceptor endpointAuthorizationInterceptor) {
        this.endpointAuthorizationInterceptor = endpointAuthorizationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(endpointAuthorizationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/internal/**", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**",
                        "/swagger-ui.html");
    }
}
