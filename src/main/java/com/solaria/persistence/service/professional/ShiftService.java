package com.solaria.persistence.service.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.professional.Shift;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.dto.request.professional.ShiftRequestDTO;
import com.solaria.persistence.dto.response.professional.ShiftResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.professional.ShiftRepository;
import com.solaria.persistence.repository.professional.TechnicianRepository;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final TechnicianRepository technicianRepository;

    public ShiftService(ShiftRepository shiftRepository, TechnicianRepository technicianRepository) {
        this.shiftRepository = shiftRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional
    public ShiftResponseDTO save(ShiftRequestDTO dto) {
        validateDateRange(dto);

        Shift shift = new Shift();
        shift.setTechnician(resolveTechnician(dto.getTechnicianId()));
        shift.setDayWeek(dto.getDayWeek());
        shift.setStartDate(dto.getStartDate());
        shift.setEndDate(dto.getEndDate());

        return toResponse(shiftRepository.save(shift));
    }

    @Transactional
    public ShiftResponseDTO update(UUID id, ShiftRequestDTO dto) {
        Shift shift = findEntityById(id);
        validateDateRange(dto);

        shift.setTechnician(resolveTechnician(dto.getTechnicianId()));
        shift.setDayWeek(dto.getDayWeek());
        shift.setStartDate(dto.getStartDate());
        shift.setEndDate(dto.getEndDate());

        return toResponse(shiftRepository.save(shift));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!shiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Turno com id:" + id + " não encontrado para exclusão");
        }
        shiftRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ShiftResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<ShiftResponseDTO> findByTechnician(UUID technicianId) {
        return shiftRepository.findByTechnicianId(technicianId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Shift findEntityById(UUID id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno não encontrado com ID: " + id));
    }

    private Technician resolveTechnician(UUID technicianId) {
        return technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Técnico não encontrado com ID: " + technicianId));
    }

    private void validateDateRange(ShiftRequestDTO dto) {
        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new BusinessRuleException("Data/hora de término deve ser posterior à data/hora de início");
        }
    }

    private ShiftResponseDTO toResponse(Shift entity) {
        ShiftResponseDTO response = new ShiftResponseDTO();
        response.setId(entity.getId());
        response.setTechnicianId(entity.getTechnician().getId());
        response.setDayWeek(entity.getDayWeek());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        return response;
    }
}
