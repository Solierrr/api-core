package com.solaria.persistence.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solaria.persistence.controller.execution.TechnicalProjectInternalController;
import com.solaria.persistence.exception.handler.GlobalExceptionHandler;
import com.solaria.persistence.exception.handler.ProblemDetailFactory;
import com.solaria.persistence.security.rbac.EndpointAuthorizationInterceptor;
import com.solaria.persistence.service.execution.ProjectRequesterAccessService;

@WebMvcTest(
        controllers = TechnicalProjectInternalController.class,
        properties = {
            "app.cors.allowed-origins=http://localhost",
            "app.jwt.jwk-set-uri=http://localhost/jwks",
            "app.jwt.issuer=test",
            "app.service-keys.api-messenger=" + InternalRequesterAccessSecurityTest.MESSENGER_KEY,
            "app.service-keys.api-auth=" + InternalRequesterAccessSecurityTest.AUTH_KEY
        })
@Import({SecurityConfig.class, ProblemDetailAuthenticationEntryPoint.class,
        ProblemDetailAccessDeniedHandler.class, ProblemDetailFactory.class, GlobalExceptionHandler.class})
class InternalRequesterAccessSecurityTest {

    static final String MESSENGER_KEY = "messenger-key-0123456789-0123456789";
    static final String AUTH_KEY = "auth-key-0123456789-0123456789-abcd";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ProjectRequesterAccessService requesterAccessService;
    @MockitoBean
    private EndpointAuthorizationInterceptor interceptor;

    private final String path =
            "/internal/technical-projects/" + UUID.randomUUID() + "/requester-access/" + UUID.randomUUID();

    @Test
    void shouldReturn401WithoutServiceKey() throws Exception {
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WithAnotherServiceKey() throws Exception {
        mvc.perform(get(path).header("X-Service-Key", AUTH_KEY)).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn204WithMessengerServiceKey() throws Exception {
        mvc.perform(get(path).header("X-Service-Key", MESSENGER_KEY)).andExpect(status().isNoContent());
    }
}
