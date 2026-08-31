package com.solaria.persistence.service.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.dto.request.catalog.ModelRequestDTO;
import com.solaria.persistence.dto.response.catalog.ModelResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.InventoryRepository;
import com.solaria.persistence.repository.catalog.ModelPhotoRepository;
import com.solaria.persistence.repository.catalog.ModelRepository;
import com.solaria.persistence.repository.catalog.OfferRepository;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private final InventoryRepository inventoryRepository;
    private final OfferRepository offerRepository;
    private final ModelPhotoRepository modelPhotoRepository;
    private final ObjectMapper objectMapper;

    public ModelService(ModelRepository modelRepository,
                        InventoryRepository inventoryRepository,
                        OfferRepository offerRepository,
                        ModelPhotoRepository modelPhotoRepository,
                        ObjectMapper objectMapper) {
        this.modelRepository = modelRepository;
        this.inventoryRepository = inventoryRepository;
        this.offerRepository = offerRepository;
        this.modelPhotoRepository = modelPhotoRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ModelResponseDTO save(ModelRequestDTO dto) {
        validateNumericFields(dto.getPowerWp(), dto.getEfficiency(), dto.getWidth(), dto.getLength(), dto.getWeight());

        Model model = new Model();
        model.setBrand(dto.getBrand());
        model.setModel(dto.getModel());
        model.setType(dto.getType());
        model.setPowerWp(dto.getPowerWp());
        model.setEfficiency(dto.getEfficiency());
        model.setWidth(dto.getWidth());
        model.setLength(dto.getLength());
        model.setWeight(dto.getWeight());

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public ModelResponseDTO update(UUID id, ModelRequestDTO dto) {
        Model model = modelRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo com id:" + id + " não encontrado(a) para atualização"));

        if (model.getStatus() != ModelStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException("Modelo não pode ser atualizado: status atual " + model.getStatus()
                    + " não permite edição (somente UNDER_ANALYSIS)");
        }

        validateNumericFields(dto.getPowerWp(), dto.getEfficiency(), dto.getWidth(), dto.getLength(), dto.getWeight());

        model.setBrand(dto.getBrand());
        model.setModel(dto.getModel());
        model.setType(dto.getType());
        model.setPowerWp(dto.getPowerWp());
        model.setEfficiency(dto.getEfficiency());
        model.setWidth(dto.getWidth());
        model.setLength(dto.getLength());
        model.setWeight(dto.getWeight());

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public ModelResponseDTO approve(UUID id) {
        Model model = modelRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo não encontrado(a) com ID: " + id));

        if (model.getStatus() != ModelStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + model.getStatus() + " → " + ModelStatus.APPROVED);
        }

        model.setStatus(ModelStatus.APPROVED);

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public ModelResponseDTO reject(UUID id) {
        Model model = modelRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo não encontrado(a) com ID: " + id));

        if (model.getStatus() != ModelStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + model.getStatus() + " → " + ModelStatus.REJECTED);
        }

        model.setStatus(ModelStatus.REJECTED);

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!modelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Modelo com id:" + id + " não encontrado(a) para exclusão");
        }

        if (inventoryRepository.existsByModelId(id) || offerRepository.existsByModelId(id)
                || modelPhotoRepository.existsByModelId(id)) {
            throw new ResourceInUseException(
                    "Modelo não pode ser excluído(a): possui estoque/oferta/foto vinculado(s)");
        }

        modelRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ModelResponseDTO findById(UUID id) {
        Model model = modelRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo não encontrado(a) com ID: " + id));
        return toResponse(model);
    }

    @Transactional(readOnly = true)
    public List<ModelResponseDTO> findAll() {
        return modelRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ModelResponseDTO> findByStatus(ModelStatus status) {
        return modelRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    private void validateNumericFields(BigDecimal powerWp, BigDecimal efficiency, BigDecimal width, BigDecimal length, BigDecimal weight) {
        if (powerWp.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Potência (Wp) inválida: " + powerWp);
        }
        if (efficiency.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Eficiência inválida: " + efficiency);
        }
        if (width.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Largura inválida: " + width);
        }
        if (length.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Comprimento inválido: " + length);
        }
        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Peso inválido: " + weight);
        }
    }

    private ModelResponseDTO toResponse(Model model) {
        return objectMapper.convertValue(model, ModelResponseDTO.class);
    }
}
