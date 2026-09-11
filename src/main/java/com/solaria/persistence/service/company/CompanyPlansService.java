package com.solaria.persistence.service.company;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.solaria.persistence.domain.entity.company.CompanyPlans;
import com.solaria.persistence.dto.response.company.CompanyPlansResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyPlansRepository;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CompanyPlansService {

    private final CompanyPlansRepository companyPlansRepository;
    private final ObjectMapper objectMapper;

    public CompanyPlansResponseDTO findById(UUID id) {
        CompanyPlans companyPlans = companyPlansRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plano não encontrado(a) com ID: " + id));
        return toResponse(companyPlans);
    }

    public List<CompanyPlansResponseDTO> findAll() {
        return companyPlansRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CompanyPlansResponseDTO toResponse(CompanyPlans companyPlans) {
        return objectMapper.convertValue(companyPlans, CompanyPlansResponseDTO.class);
    }
}
