package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.solaria.persistence.domain.entity.professional.Profession;
import com.solaria.persistence.dto.response.professional.ProfessionResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.ProfessionRepository;
import lombok.RequiredArgsConstructor;
import com.solaria.persistence.security.CurrentUserService;

@Service
@RequiredArgsConstructor
public class ProfessionService {

    private final ProfessionRepository professionRepository;
    private final CurrentUserService currentUserService;

    public ProfessionResponseDTO findById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        Profession profession = professionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Profissão não encontrado(a) com ID: " + id));
        return toResponse(profession);
    }

    public List<ProfessionResponseDTO> findAll() {
        currentUserService.getProvisionedCurrentUserId();
        return professionRepository.findAll().stream().map(this::toResponse).toList();
    }

    private ProfessionResponseDTO toResponse(Profession profession) {
        ProfessionResponseDTO response = new ProfessionResponseDTO();
        response.setId(profession.getId());
        response.setName(profession.getName());
        response.setRequiresRegistration(profession.getRequiresRegistration());
        return response;
    }
}
