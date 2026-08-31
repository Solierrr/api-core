package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.professional.Profession;
import com.solaria.persistence.dto.request.professional.ProfessionRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionResponseDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.ProfessionRepository;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;

@Service
public class ProfessionService {

    private final ProfessionRepository professionRepository;
    private final ProfessionalRegistrationRepository professionalRegistrationRepository;
    private final ObjectMapper objectMapper;

    public ProfessionService(ProfessionRepository professionRepository,
                             ProfessionalRegistrationRepository professionalRegistrationRepository,
                             ObjectMapper objectMapper) {
        this.professionRepository = professionRepository;
        this.professionalRegistrationRepository = professionalRegistrationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProfessionResponseDTO save(ProfessionRequestDTO dto) {
        Profession profession = new Profession();
        profession.setName(dto.getName());
        profession.setRequiresRegistration(dto.getRequiresRegistration());

        return toResponse(professionRepository.save(profession));
    }

    @Transactional
    public ProfessionResponseDTO update(UUID id, ProfessionRequestDTO dto) {
        Profession profession = professionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Profissão com id:" + id + " não encontrada para atualização"));

        profession.setName(dto.getName());
        profession.setRequiresRegistration(dto.getRequiresRegistration());

        return toResponse(professionRepository.save(profession));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!professionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profissão com id:" + id + " não encontrada para exclusão");
        }

        if (professionalRegistrationRepository.existsByProfessionId(id)) {
            throw new ResourceInUseException("Profissão não pode ser excluída: possui registro profissional vinculado");
        }

        professionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProfessionResponseDTO findById(UUID id) {
        Profession profession = professionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Profissão não encontrada com ID: " + id));
        return toResponse(profession);
    }

    @Transactional(readOnly = true)
    public List<ProfessionResponseDTO> findAll() {
        return professionRepository.findAll().stream().map(this::toResponse).toList();
    }

    private ProfessionResponseDTO toResponse(Profession profession) {
        return objectMapper.convertValue(profession, ProfessionResponseDTO.class);
    }
}
