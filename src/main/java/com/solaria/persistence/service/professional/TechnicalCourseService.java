package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.professional.TechnicalCourse;
import com.solaria.persistence.dto.request.professional.TechnicalCourseRequestDTO;
import com.solaria.persistence.dto.response.professional.TechnicalCourseResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.professional.TechnicalCourseRepository;

@Service
public class TechnicalCourseService {

    private final TechnicalCourseRepository technicalCourseRepository;
    private final CompanyRepository companyRepository;

    public TechnicalCourseService(TechnicalCourseRepository technicalCourseRepository,
                                  CompanyRepository companyRepository) {
        this.technicalCourseRepository = technicalCourseRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public TechnicalCourseResponseDTO save(TechnicalCourseRequestDTO dto) {
        TechnicalCourse technicalCourse = new TechnicalCourse();
        technicalCourse.setCompany(resolveCompany(dto.getCompanyId()));
        technicalCourse.setTitle(dto.getTitle());
        technicalCourse.setInformation(dto.getInformation());
        technicalCourse.setLink(dto.getLink());

        return toResponse(technicalCourseRepository.save(technicalCourse));
    }

    @Transactional
    public TechnicalCourseResponseDTO update(UUID id, TechnicalCourseRequestDTO dto) {
        TechnicalCourse technicalCourse = technicalCourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso Técnico com id:" + id + " não encontrado para atualização"));

        technicalCourse.setCompany(resolveCompany(dto.getCompanyId()));
        technicalCourse.setTitle(dto.getTitle());
        technicalCourse.setInformation(dto.getInformation());
        technicalCourse.setLink(dto.getLink());

        return toResponse(technicalCourseRepository.save(technicalCourse));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!technicalCourseRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Curso Técnico com id:" + id + " não encontrado para exclusão");
        }
        technicalCourseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TechnicalCourseResponseDTO findById(UUID id) {
        TechnicalCourse technicalCourse = technicalCourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso Técnico não encontrado com ID: " + id));
        return toResponse(technicalCourse);
    }

    @Transactional(readOnly = true)
    public TechnicalCourseResponseDTO findById(UUID id, UUID companyId) {
        TechnicalCourse technicalCourse = technicalCourseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso Técnico não encontrado com ID: " + id));
        return toResponse(technicalCourse);
    }

    @Transactional(readOnly = true)
    public List<TechnicalCourseResponseDTO> findByCompany(UUID companyId) {
        return technicalCourseRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Company resolveCompany(UUID companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada com ID: " + companyId));
    }

    private TechnicalCourseResponseDTO toResponse(TechnicalCourse entity) {
        TechnicalCourseResponseDTO response = new TechnicalCourseResponseDTO();
        response.setId(entity.getId());
        response.setCompanyId(entity.getCompany() == null ? null : entity.getCompany().getId());
        response.setTitle(entity.getTitle());
        response.setInformation(entity.getInformation());
        response.setLink(entity.getLink());
        return response;
    }
}
