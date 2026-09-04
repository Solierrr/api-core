package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.professional.Certification;
import com.solaria.persistence.dto.request.professional.CertificationRequestDTO;
import com.solaria.persistence.dto.response.professional.CertificationResponseDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.CertificationRecordRepository;
import com.solaria.persistence.repository.professional.CertificationRepository;

@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final CertificationRecordRepository certificationRecordRepository;
    private final ObjectMapper objectMapper;

    public CertificationService(CertificationRepository certificationRepository,
                                CertificationRecordRepository certificationRecordRepository,
                                ObjectMapper objectMapper) {
        this.certificationRepository = certificationRepository;
        this.certificationRecordRepository = certificationRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CertificationResponseDTO save(CertificationRequestDTO dto) {
        Certification certification = new Certification();
        certification.setName(dto.getName());
        certification.setIssuer(dto.getIssuer());
        certification.setValidity(dto.getValidity());
        certification.setDescription(dto.getDescription());

        return toResponse(certificationRepository.save(certification));
    }

    @Transactional
    public CertificationResponseDTO update(UUID id, CertificationRequestDTO dto) {
        Certification certification = certificationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Certificação com id:" + id + " não encontrada para atualização"));

        certification.setName(dto.getName());
        certification.setIssuer(dto.getIssuer());
        certification.setValidity(dto.getValidity());
        certification.setDescription(dto.getDescription());

        return toResponse(certificationRepository.save(certification));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!certificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Certificação com id:" + id + " não encontrada para exclusão");
        }

        if (certificationRecordRepository.existsByCertificationId(id)) {
            throw new ResourceInUseException("Certificação não pode ser excluída: possui registro de certificação vinculado");
        }

        certificationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CertificationResponseDTO findById(UUID id) {
        Certification certification = certificationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Certificação não encontrada com ID: " + id));
        return toResponse(certification);
    }

    @Transactional(readOnly = true)
    public List<CertificationResponseDTO> findAll() {
        return certificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CertificationResponseDTO toResponse(Certification certification) {
        return objectMapper.convertValue(certification, CertificationResponseDTO.class);
    }
}
