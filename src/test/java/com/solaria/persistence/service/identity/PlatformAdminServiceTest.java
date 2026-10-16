package com.solaria.persistence.service.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solaria.persistence.domain.entity.identity.PlatformAdmin;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.dto.response.identity.PlatformAdminResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.PlatformAdminRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class PlatformAdminServiceTest {

    @Mock private PlatformAdminRepository platformAdminRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserService currentUserService;

    private PlatformAdminService service;

    private User currentUser;

    @BeforeEach
    void setUp() {
        service = new PlatformAdminService(platformAdminRepository, userRepository, currentUserService);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
    }

    @Test
    void findMeReturnsNullWhenCurrentUserIsNotAPlatformAdmin() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(platformAdminRepository.findByUserId(currentUser.getId())).thenReturn(Optional.empty());

        assertNull(service.findMe());
    }

    @Test
    void findMeReturnsTheAdminRecordWhenCurrentUserIsAPlatformAdmin() {
        PlatformAdmin admin = new PlatformAdmin();
        admin.setId(UUID.randomUUID());
        admin.setUser(currentUser);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(platformAdminRepository.findByUserId(currentUser.getId())).thenReturn(Optional.of(admin));

        PlatformAdminResponseDTO response = service.findMe();

        assertEquals(admin.getId(), response.id());
        assertEquals(currentUser.getId(), response.userId());
    }

    @Test
    void grantPromotesATargetUserWhenCallerIsAlreadyAPlatformAdmin() {
        User target = new User();
        target.setId(UUID.randomUUID());

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(platformAdminRepository.existsByUserId(currentUser.getId())).thenReturn(true);
        when(platformAdminRepository.existsByUserId(target.getId())).thenReturn(false);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(platformAdminRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    PlatformAdmin saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        PlatformAdminResponseDTO response = service.grant(target.getId());

        assertEquals(target.getId(), response.userId());
    }

    @Test
    void grantRejectsACallerThatIsNotAPlatformAdmin() {
        UUID targetId = UUID.randomUUID();

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(platformAdminRepository.existsByUserId(currentUser.getId())).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> service.grant(targetId));
    }

    @Test
    void grantRejectsATargetThatIsAlreadyAPlatformAdmin() {
        UUID targetId = UUID.randomUUID();

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(platformAdminRepository.existsByUserId(currentUser.getId())).thenReturn(true);
        when(platformAdminRepository.existsByUserId(targetId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.grant(targetId));
    }

    @Test
    void grantRejectsAnUnknownTargetUser() {
        UUID targetId = UUID.randomUUID();

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(platformAdminRepository.existsByUserId(currentUser.getId())).thenReturn(true);
        when(platformAdminRepository.existsByUserId(targetId)).thenReturn(false);
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.grant(targetId));
    }
}
