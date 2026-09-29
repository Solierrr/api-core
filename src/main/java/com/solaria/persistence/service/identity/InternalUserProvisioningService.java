package com.solaria.persistence.service.identity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.dto.response.identity.InternalUserProvisionResponseDTO;
import com.solaria.persistence.repository.identity.UserRepository;

// classe service dedicada a eventos de registro de Users vindos de api-auth (M2M)
// separado de UserService pois fluxo prexisa ser idempotente; entrega de eventos em fila
@Service
public class InternalUserProvisioningService {

    // único repositório necessário, toda a lógica de idempotência gira em torno de auth_id
    private final UserRepository userRepository;

    public InternalUserProvisioningService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public InternalUserProvisionResponseDTO provision(UUID authId) {
        // busca um User já criado para este authId antes de tentar criar
        Optional<User> existing = userRepository.findByAuthId(authId);
        if (existing.isPresent()) {
            // já criado -> devolve o registro atual, created=false
            return toResponse(existing.get(), false);
        }

        // O evento de registro e GET /users/me podem chegar simultaneamente.
        // ON CONFLICT mantém a transação utilizável para ler o registro vencedor.
        String username = "u_" + authId.toString().replace("-", "").substring(0, 28);
        boolean created = userRepository.insertProvisionedUser(UUID.randomUUID(), authId, username) == 1;
        User user = userRepository.findByAuthId(authId).orElseThrow();
        return toResponse(user, created);
    }

    private InternalUserProvisionResponseDTO toResponse(User user, boolean created) {
        return InternalUserProvisionResponseDTO.builder()
                .id(user.getId())
                .authId(user.getAuth_id())
                .created(created)
                .build();
    }
}
