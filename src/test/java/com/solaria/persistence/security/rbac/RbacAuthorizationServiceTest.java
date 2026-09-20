package com.solaria.persistence.security.rbac;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.repository.PositionPermissionRepository;
import com.solaria.persistence.repository.UserCompanyRepository;
import com.solaria.persistence.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class RbacAuthorizationServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserCompanyRepository userCompanyRepository;

    @Mock
    private PositionPermissionRepository positionPermissionRepository;

    private RbacAuthorizationService rbac;

    @BeforeEach
    void setUp() {
        rbac = new RbacAuthorizationService(
                currentUserService, userCompanyRepository, positionPermissionRepository);
    }

    @Test
    void shouldAllowAuthenticatedUserConnectionEndpointsWithoutCompanyRbac() {
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("POST /api/connections"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("GET /api/connections"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("GET /api/connections/{id}"));
        assertDoesNotThrow(() -> rbac.requireEndpointAccess("DELETE /api/connections/{id}"));

        verifyNoInteractions(currentUserService, userCompanyRepository, positionPermissionRepository);
    }
}
