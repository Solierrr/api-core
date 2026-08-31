package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.professional.Certification;
import com.solaria.persistence.domain.entity.professional.CertificationRecord;
import com.solaria.persistence.domain.entity.professional.ProfessionalRegistration;
import com.solaria.persistence.dto.request.professional.CertificationRecordRequestDTO;
import com.solaria.persistence.dto.response.professional.CertificationRecordResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.CertificationRecordRepository;
import com.solaria.persistence.repository.professional.CertificationRepository;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;


@Service
public class CertificationRecordService {

    private final CertificationRecordRepository certificationRecordRepository;
    private final ProfessionalRegistrationRepository professionalRegistrationRepository;
    private final CertificationRepository certificationRepository;

    public CertificationRecordService(CertificationRecordRepository certificationRecordRepository,
                                      ProfessionalRegistrationRepository professionalRegistrationRepository,
                                      CertificationRepository certificationRepository) {
        this.certificationRecordRepository = certificationRecordRepository;
        this.professionalRegistrationRepository = professionalRegistrationRepository;
        this.certificationRepository = certificationRepository;
    }

    @Transactional
    public CertificationRecordResponseDTO save(CertificationRecordRequestDTO dto) {
        CertificationRecord certificationRecord = new CertificationRecord();
        certificationRecord.setProfessionalRegistration(resolveProfessionalRegistration(dto.getProfessionalRegistrationId()));
        certificationRecord.setCertification(resolveCertification(dto.getCertificationId()));

        return toResponse(certificationRecordRepository.save(certificationRecord));
    }

    @Transactional
    public CertificationRecordResponseDTO update(UUID id, CertificationRecordRequestDTO dto) {
        CertificationRecord certificationRecord = certificationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro de Certificação com id:" + id + " não encontrado para atualização"));

        certificationRecord.setProfessionalRegistration(resolveProfessionalRegistration(dto.getProfessionalRegistrationId()));
        certificationRecord.setCertification(resolveCertification(dto.getCertificationId()));

        return toResponse(certificationRecordRepository.save(certificationRecord));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!certificationRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Registro de Certificação com id:" + id + " não encontrado para exclusão");
        }
        certificationRecordRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CertificationRecordResponseDTO findById(UUID id) {
        CertificationRecord certificationRecord = certificationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro de Certificação não encontrado com ID: " + id));
        return toResponse(certificationRecord);
    }

    @Transactional(readOnly = true)
    public List<CertificationRecordResponseDTO> findByProfessionalRegistration(UUID professionalRegistrationId) {
        return certificationRecordRepository.findByProfessionalRegistrationId(professionalRegistrationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProfessionalRegistration resolveProfessionalRegistration(UUID professionalRegistrationId) {
        if (professionalRegistrationId == null) {
            return null;
        }
        return professionalRegistrationRepository.findById(professionalRegistrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registro Profissional não encontrado com ID: " + professionalRegistrationId));
    }

    private Certification resolveCertification(UUID certificationId) {
        if (certificationId == null) {
            return null;
        }
        return certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificação não encontrada com ID: " + certificationId));
    }

    private CertificationRecordResponseDTO toResponse(CertificationRecord entity) {
        CertificationRecordResponseDTO response = new CertificationRecordResponseDTO();
        response.setId(entity.getId());
        response.setProfessionalRegistrationId(
                entity.getProfessionalRegistration() == null ? null : entity.getProfessionalRegistration().getId());
        response.setCertificationId(entity.getCertification() == null ? null : entity.getCertification().getId());
        return response;
    }
}
