package com.solaria.persistence.service;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.repository.identity.UserCompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import com.solaria.persistence.repository.identity.PersonRepository;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionVisibilityTest {
    private final UserCompanyRepository memberships = mock(UserCompanyRepository.class);
    private final PersonRepository persons = mock(PersonRepository.class);
    private final ConnectionVisibility visibility = new ConnectionVisibility(memberships, persons);

    @BeforeEach void personalProfiles() {
        lenient().when(persons.existsByUserId(any())).thenReturn(true);
    }

    @Test void independentUsersCanConnectWithoutCompanyMembership() {
        assertTrue(visibility.visible(user(null), user(null)));
    }
    @Test void managedUserCannotConnectOutsideCompany() {
        assertFalse(visibility.visible(user(UUID.randomUUID()), user(null)));
    }
    @Test void managedUserCanConnectToIndependentCoworker() {
        var company = UUID.randomUUID();
        var target = user(null);
        when(memberships.existsByUserIdAndCompanyId(target.getId(), company)).thenReturn(true);
        assertTrue(visibility.visible(user(company), target));
    }
    @Test void outsidersCannotAccessManagedProfiles() {
        assertFalse(visibility.visible(user(null), user(UUID.randomUUID())));
    }
    @Test void sameCompanyManagedUsersAreVisible() {
        var company = UUID.randomUUID();
        assertTrue(visibility.visible(user(company), user(company)));
    }
    @Test void selfAndInactiveUsersAreHidden() {
        var owner = user(null);
        assertFalse(visibility.visible(owner, owner));
        var target = user(null); target.setActive(false);
        assertFalse(visibility.visible(owner, target));
    }
    private User user(UUID company) {
        var user = new User(); user.setId(UUID.randomUUID()); user.setActive(true);
        user.setManagedByCompanyId(company); return user;
    }
}
