package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.solaria.persistence.domain.entity.professional.Certification;
import com.solaria.persistence.dto.response.professional.CertificationResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.CertificationRepository;
import lombok.RequiredArgsConstructor;
import com.solaria.persistence.security.CurrentUserService;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final CurrentUserService currentUserService;

    public CertificationResponseDTO findById(UUID id) {
        currentUserService.getProvisionedCurrentUserId();
        Certification certification = certificationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Certificação não encontrado(a) com ID: " + id));
        return toResponse(certification);
    }

    public List<CertificationResponseDTO> findAll() {
        currentUserService.getProvisionedCurrentUserId();
        return certificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CertificationResponseDTO toResponse(Certification certification) {
        CertificationResponseDTO response = new CertificationResponseDTO();
        response.setId(certification.getId());
        response.setName(certification.getName());
        response.setIssuer(certification.getIssuer());
        response.setValidity(certification.getValidity());
        response.setDescription(certification.getDescription());
        return response;
    }
}
