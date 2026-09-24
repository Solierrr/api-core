package com.solaria.persistence.service.professional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.professional.ProfessionalReview;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.dto.request.professional.ProfessionalReviewRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionalReviewResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.ProfessionalReviewRepository;
import com.solaria.persistence.repository.execution.ServiceExecutorRepository;
import com.solaria.persistence.repository.execution.TechnicalServiceRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;
import com.solaria.persistence.repository.identity.UserRepository;

@Service
public class ProfessionalReviewService {

    private static final BigDecimal MIN_RATING = BigDecimal.ZERO;
    private static final BigDecimal MAX_RATING = BigDecimal.valueOf(5);

    private final ProfessionalReviewRepository professionalReviewRepository;
    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final TechnicalServiceRepository technicalServiceRepository;
    private final ServiceExecutorRepository serviceExecutorRepository;

    public ProfessionalReviewService(ProfessionalReviewRepository professionalReviewRepository,
                                     TechnicianRepository technicianRepository,
                                     UserRepository userRepository,
                                     TechnicalServiceRepository technicalServiceRepository,
                                     ServiceExecutorRepository serviceExecutorRepository) {
        this.professionalReviewRepository = professionalReviewRepository;
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
        this.technicalServiceRepository = technicalServiceRepository;
        this.serviceExecutorRepository = serviceExecutorRepository;
    }

    @Transactional
    public ProfessionalReviewResponseDTO save(ProfessionalReviewRequestDTO dto) {
        if (dto.getRating() == null
                || dto.getRating().compareTo(MIN_RATING) < 0
                || dto.getRating().compareTo(MAX_RATING) > 0) {
            throw new InvalidFieldException("Nota inválida (esperado 0-5): " + dto.getRating());
        }

        Technician professional = technicianRepository.findById(dto.getProfessionalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profissional não encontrado com ID: " + dto.getProfessionalId()));

        User reviewer = userRepository.findById(dto.getReviewerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Avaliador não encontrado com ID: " + dto.getReviewerId()));

        TechnicalService service = technicalServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviço não encontrado com ID: " + dto.getServiceId()));

        if (service.getStatus() != ServiceStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Avaliação só é permitida para serviço concluído (status atual: " + service.getStatus() + ")");
        }

        if (!serviceExecutorRepository.existsByService_IdAndTechnicianAffiliation_Technician_Id(
                dto.getServiceId(), dto.getProfessionalId())) {
            throw new BusinessRuleException(
                    "Profissional " + dto.getProfessionalId() + " não executou o serviço " + dto.getServiceId());
        }

        if (professionalReviewRepository.existsByReviewer_IdAndProfessional_IdAndService_Id(
                dto.getReviewerId(), dto.getProfessionalId(), dto.getServiceId())) {
            throw new DuplicateResourceException(
                    "Avaliador já avaliou este profissional para este serviço");
        }

        ProfessionalReview review = new ProfessionalReview();
        review.setProfessional(professional);
        review.setReviewer(reviewer);
        review.setService(service);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setActive(true);
        review.setCreatedAt(OffsetDateTime.now());

        return toResponse(professionalReviewRepository.save(review));
    }

    @Transactional
    public ProfessionalReviewResponseDTO deactivate(UUID id) {
        ProfessionalReview review = professionalReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Avaliação não encontrada com ID: " + id));
        review.setActive(false);
        return toResponse(professionalReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public ProfessionalReviewResponseDTO findById(UUID id) {
        return toResponse(professionalReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Avaliação não encontrada com ID: " + id)));
    }

    @Transactional(readOnly = true)
    public List<ProfessionalReviewResponseDTO> findByProfessional(UUID professionalId) {
        return professionalReviewRepository.findByProfessional_IdAndActiveTrue(professionalId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProfessionalReviewResponseDTO toResponse(ProfessionalReview entity) {
        return ProfessionalReviewResponseDTO.builder()
                .id(entity.getId())
                .professionalId(entity.getProfessional().getId())
                .reviewerId(entity.getReviewer().getId())
                .serviceId(entity.getService().getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
