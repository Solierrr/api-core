package com.solaria.persistence.service.professional;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.repository.professional.ProfessionalOnboardingRepository;

@Service
public class ApprovedProfessionalGuard {
    private final UserRepository users;
    private final ProfessionalOnboardingRepository profiles;

    public ApprovedProfessionalGuard(UserRepository users, ProfessionalOnboardingRepository profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    public void requireLegacyChangesAllowed(UUID userId) {
        var user = users.findById(userId).orElseThrow();
        if (profiles.lockByAuthId(user.getAuth_id()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Submit changes through professional onboarding for review");
        }
    }
}
