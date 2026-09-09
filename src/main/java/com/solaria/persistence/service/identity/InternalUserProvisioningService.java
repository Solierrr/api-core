package com.solaria.persistence.service.identity;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.dto.response.identity.InternalUserProvisionResponseDTO;
import com.solaria.persistence.repository.identity.UserRepository;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InternalUserProvisioningService {

  private final UserRepository userRepository;

  public InternalUserProvisioningService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public InternalUserProvisionResponseDTO provision(UUID authId) {
    Optional<User> existing = userRepository.findByAuthId(authId);
    if (existing.isPresent()) {
      return toResponse(existing.get(), false);
    }

    User user = new User();
    user.setAuth_id(authId);
    user.setUsername(availableUsername(authId));
    return toResponse(userRepository.saveAndFlush(user), true);
  }

  private String availableUsername(UUID authId) {
    String candidate = encodeUsername(authId);
    if (!userRepository.existsByUsername(candidate)) {
      return candidate;
    }
    for (int attempt = 0; attempt < 3; attempt++) {
      candidate = encodeUsername(UUID.randomUUID());
      if (!userRepository.existsByUsername(candidate)) {
        return candidate;
      }
    }
    throw new DataIntegrityViolationException("Não foi possível gerar um username disponível.");
  }

  private String encodeUsername(UUID identity) {
    byte[] bytes = ByteBuffer.allocate(16)
        .putLong(identity.getMostSignificantBits())
        .putLong(identity.getLeastSignificantBits())
        .array();
    String encoded = new BigInteger(1, bytes).toString(36);
    return "u_" + "0".repeat(25 - encoded.length()) + encoded;
  }

  private InternalUserProvisionResponseDTO toResponse(User user, boolean created) {
    return InternalUserProvisionResponseDTO.builder()
        .id(user.getId())
        .authId(user.getAuth_id())
        .created(created)
        .build();
  }
}
