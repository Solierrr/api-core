package com.solaria.persistence.service.company;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.company.CompanyPlans;
import com.solaria.persistence.dto.request.company.CompanyPlansRequestDTO;
import com.solaria.persistence.dto.response.company.CompanyPlansResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.company.CompanyPlansRepository;
import com.solaria.persistence.repository.company.SubscriptionRepository;

@Service
public class CompanyPlansService {

    private final CompanyPlansRepository companyPlansRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public CompanyPlansService(CompanyPlansRepository companyPlansRepository,
                               SubscriptionRepository subscriptionRepository,
                               ObjectMapper objectMapper) {
        this.companyPlansRepository = companyPlansRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CompanyPlansResponseDTO save(CompanyPlansRequestDTO dto) {
        validateValue(dto.getValue());

        CompanyPlans companyPlans = new CompanyPlans();
        companyPlans.setName(dto.getName());
        companyPlans.setValue(dto.getValue());
        companyPlans.setCycle(dto.getCycle());

        return toResponse(companyPlansRepository.save(companyPlans));
    }

    @Transactional
    public CompanyPlansResponseDTO update(UUID id, CompanyPlansRequestDTO dto) {
        CompanyPlans companyPlans = companyPlansRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plano com id:" + id + " não encontrado(a) para atualização"));

        validateValue(dto.getValue());

        companyPlans.setName(dto.getName());
        companyPlans.setValue(dto.getValue());
        companyPlans.setCycle(dto.getCycle());

        return toResponse(companyPlansRepository.save(companyPlans));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!companyPlansRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plano com id:" + id + " não encontrado(a) para exclusão");
        }

        if (subscriptionRepository.existsByPlanId(id)) {
            throw new ResourceInUseException("Plano não pode ser excluído(a): possui assinatura(s) vinculada(s)");
        }

        companyPlansRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CompanyPlansResponseDTO findById(UUID id) {
        CompanyPlans companyPlans = companyPlansRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plano não encontrado(a) com ID: " + id));
        return toResponse(companyPlans);
    }

    @Transactional(readOnly = true)
    public List<CompanyPlansResponseDTO> findAll() {
        return companyPlansRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void validateValue(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Valor do plano inválido: " + value);
        }
    }

    private CompanyPlansResponseDTO toResponse(CompanyPlans companyPlans) {
        return objectMapper.convertValue(companyPlans, CompanyPlansResponseDTO.class);
    }
}
