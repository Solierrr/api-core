package com.solaria.persistence.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class ServiceKeyAuthFilterTest {

    private static final String KEY = "unit-test-service-key-0123456789abcdef";

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private Authentication run(String configuredKey, String header) throws Exception {
        ServiceKeyProperties props = new ServiceKeyProperties();
        props.put("api-auth", configuredKey);
        ServiceKeyAuthFilter filter = new ServiceKeyAuthFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/internal/test");
        if (header != null) {
            request.addHeader("X-Service-Key", header);
        }
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void authenticatesKnownKeyAsServiceName() throws Exception {
        Authentication auth = run(KEY, KEY);
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("api-auth");
        assertThat(auth.getAuthorities()).extracting(Object::toString)
                .containsExactly("ROLE_SERVICE", "SERVICE_api-auth");
    }

    @Test
    void ignoresUnknownKey() throws Exception {
        assertThat(run(KEY, "zz")).isNull();
    }

    @Test
    void blankConfiguredKeyNeverMatches() throws Exception {
        assertThat(run("", "")).isNull();
    }

    @Test
    void missingHeaderLeavesContextEmpty() throws Exception {
        assertThat(run(KEY, null)).isNull();
        assertThat(run("", null)).isNull();
    }

    @Test
    void rejectsShortConfiguredKey() {
        ServiceKeyProperties props = new ServiceKeyProperties();
        props.put("api-auth", "short");
        assertThatThrownBy(props::validate).isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining("short");
    }
}
