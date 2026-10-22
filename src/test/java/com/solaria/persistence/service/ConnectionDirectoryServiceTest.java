package com.solaria.persistence.service;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConnectionDirectoryServiceTest {
    @Test
    void inactiveOwnerCannotSearchPeople() {
        var users = mock(UserRepository.class);
        var current = mock(CurrentUserService.class);
        var owner = new User();
        owner.setActive(false);
        when(current.getCurrentUser()).thenReturn(owner);
        var service = new ConnectionDirectoryService(users, current);
        assertThrows(UnauthorizedAccessException.class, () -> service.search("", 0, false));
        verifyNoInteractions(users);
    }

    @Test
    void inactiveOwnerCannotOpenProfile() {
        var users = mock(UserRepository.class);
        var current = mock(CurrentUserService.class);
        var owner = new User();
        owner.setActive(false);
        when(current.getCurrentUser()).thenReturn(owner);
        var service = new ConnectionDirectoryService(users, current);
        assertThrows(UnauthorizedAccessException.class, () -> service.profile(UUID.randomUUID()));
        verifyNoInteractions(users);
    }
}
