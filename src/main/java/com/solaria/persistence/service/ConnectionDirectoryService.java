package com.solaria.persistence.service;

import com.solaria.persistence.dto.response.ConnectionPersonResponse;
import com.solaria.persistence.dto.response.ConnectionPeopleResponse;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.identity.UserRepository;
import com.solaria.persistence.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConnectionDirectoryService {
    private final UserRepository users;
    private final CurrentUserService current;

    public ConnectionPeopleResponse search(String query, int page, boolean connectedOnly) {
        var owner = activeOwner();
        var ids = owner.getConnections() == null ? List.<UUID>of() : owner.getConnections();
        var text = query.trim().toLowerCase(java.util.Locale.ROOT);
        var result = connectedOnly
                ? users.findConnectedPeople(owner.getId(), owner.getManagedByCompanyId(), text, PageRequest.of(page, 20))
                : users.findConnectionPeople(owner.getId(), owner.getManagedByCompanyId(), text, PageRequest.of(page, 20));
        return new ConnectionPeopleResponse(result.getContent().stream().map(row -> response(row, ids)).toList(), result.hasNext());
    }

    public ConnectionPersonResponse profile(UUID id) {
        var owner = activeOwner();
        var row = users.findVisiblePerson(owner.getId(), owner.getManagedByCompanyId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não disponível"));
        return response(row, owner.getConnections() == null ? List.of() : owner.getConnections());
    }

    private com.solaria.persistence.domain.entity.identity.User activeOwner() {
        var owner = current.getCurrentUser();
        if (!Boolean.TRUE.equals(owner.getActive()))
            throw new com.solaria.persistence.exception.UnauthorizedAccessException("Conta inativa");
        return owner;
    }

    private ConnectionPersonResponse response(UserRepository.ConnectionPerson row, List<UUID> ids) {
        return new ConnectionPersonResponse(row.getId(), row.getName(), row.getUsername(),
                row.getAvatar(), row.getBanner(), ids.contains(row.getId()));
    }
}
