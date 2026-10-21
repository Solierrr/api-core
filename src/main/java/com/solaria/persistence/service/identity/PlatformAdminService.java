package com.solaria.persistence.service.identity;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.identity.PlatformAdmin;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.dto.response.identity.PlatformAdminResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.identity.PlatformAdminRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.CurrentUserService;

@Service
public class PlatformAdminService {

    private final PlatformAdminRepository platformAdminRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public PlatformAdminService(PlatformAdminRepository platformAdminRepository,
                                 UserRepository userRepository,
                                 CurrentUserService currentUserService) {
        this.platformAdminRepository = platformAdminRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public PlatformAdminResponseDTO findMe() {
        User user = currentUserService.getCurrentUser();
        return platformAdminRepository.findByUserId(user.getId())
                .map(admin -> new PlatformAdminResponseDTO(admin.getId(), user.getId()))
                .orElse(null);
    }

    // Só um Admin Solaria já existente pode promover outro — evita autopromoção.
    @Transactional
    public PlatformAdminResponseDTO grant(UUID targetUserId) {
        User currentUser = currentUserService.getCurrentUser();
        if (!platformAdminRepository.existsByUserId(currentUser.getId())) {
            throw new UnauthorizedAccessException("Somente um Admin Solaria pode promover outro");
        }
        if (platformAdminRepository.existsByUserId(targetUserId)) {
            throw new DuplicateResourceException("Usuário já é Admin Solaria");
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        PlatformAdmin admin = new PlatformAdmin();
        admin.setUser(target);
        PlatformAdmin saved = platformAdminRepository.save(admin);
        return new PlatformAdminResponseDTO(saved.getId(), target.getId());
    }
}
