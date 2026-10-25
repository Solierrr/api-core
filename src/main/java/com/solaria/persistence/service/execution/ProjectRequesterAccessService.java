package com.solaria.persistence.service.execution;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.identity.UserRepository;

/**
 * Verifica se um usuário (identificado pelo authId) é o demandante de um projeto técnico
 */
@Service
public class ProjectRequesterAccessService {

    private final TechnicalProjectRepository technicalProjectRepository;
    private final UserRepository userRepository;
    private final ProjectAccess projectAccess;

    public ProjectRequesterAccessService(TechnicalProjectRepository technicalProjectRepository,
                                         UserRepository userRepository,
                                         ProjectAccess projectAccess) {
        this.technicalProjectRepository = technicalProjectRepository;
        this.userRepository = userRepository;
        this.projectAccess = projectAccess;
    }

    /**
     * Demandante é o dono do projeto ou o ADMIN da empresa dona, com a conta ativa
     *
     * @throws ResourceNotFoundException projeto ou usuário (authId) inexistente
     * @throws UnauthorizedAccessException usuário não é o demandante do projeto
     */
    @Transactional(readOnly = true)
    public void requireRequester(UUID projectId, UUID authId) {
        TechnicalProject project = technicalProjectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto Técnico não encontrado com ID: " + projectId));
        User user = userRepository.findByAuthId(authId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com authId: " + authId));
        if (!Boolean.TRUE.equals(user.getActive()) || !projectAccess.isOwner(project, user)) {
            throw new UnauthorizedAccessException("Apenas o demandante do projeto pode executar esta ação.");
        }
    }
}
