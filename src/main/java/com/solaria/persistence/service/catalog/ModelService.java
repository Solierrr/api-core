package com.solaria.persistence.service.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.domain.enums.catalog.ProductType;
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
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.exception.UnauthorizedAccessException;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private final InventoryRepository inventoryRepository;
    private final OfferRepository offerRepository;
    private final ModelPhotoRepository modelPhotoRepository;
    private final RbacAuthorizationService rbac;
    private final BrandService brands;

    public ModelService(ModelRepository modelRepository,
                        InventoryRepository inventoryRepository,
                        OfferRepository offerRepository,
                        ModelPhotoRepository modelPhotoRepository,
                        RbacAuthorizationService rbac, BrandService brands) {
        this.modelRepository = modelRepository;
        this.inventoryRepository = inventoryRepository;
        this.offerRepository = offerRepository;
        this.modelPhotoRepository = modelPhotoRepository;
        this.rbac = rbac;
        this.brands = brands;
    }

    @Transactional
    public ModelResponseDTO save(ModelRequestDTO dto) {
        validateProductFields(dto);

        Model model = new Model();
        model.setCreatorCompanyId(rbac.getCurrentCompanyId());
        model.setBrandEntry(brands.selectable(dto.getBrandId(), rbac.getCurrentCompanyId()));
        applyProductFields(model, dto);

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public ModelResponseDTO update(UUID id, ModelRequestDTO dto) {
        Model model = modelRepository.lockById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo com id:" + id + " não encontrado(a) para atualização"));
        requireOwner(model);

        if (model.getStatus() != ModelStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException("Modelo não pode ser atualizado: status atual " + model.getStatus()
                    + " não permite edição (somente UNDER_ANALYSIS)", "MODEL_NOT_EDITABLE");
        }

        validateProductFields(dto);

        if (!java.util.Objects.equals(model.getBrandId(), dto.getBrandId())) {
            model.setBrandEntry(brands.selectable(dto.getBrandId(), rbac.getCurrentCompanyId()));
        }
        applyProductFields(model, dto);

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public ModelResponseDTO approve(UUID id) {
        Model model = modelRepository.lockById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo não encontrado(a) com ID: " + id));

        if (model.getStatus() != ModelStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + model.getStatus() + " → " + ModelStatus.APPROVED, "MODEL_INVALID_STATUS_TRANSITION");
        }

        model.setStatus(ModelStatus.APPROVED);

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public ModelResponseDTO reject(UUID id) {
        Model model = modelRepository.lockById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo não encontrado(a) com ID: " + id));

        if (model.getStatus() != ModelStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + model.getStatus() + " → " + ModelStatus.REJECTED, "MODEL_INVALID_STATUS_TRANSITION");
        }

        model.setStatus(ModelStatus.REJECTED);

        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public void deleteById(UUID id) {
        Model model = modelRepository.lockById(id).orElseThrow(
                () -> new ResourceNotFoundException("Modelo com id:" + id + " não encontrado(a) para exclusão"));
        requireOwner(model);
        if (model.getStatus() != ModelStatus.UNDER_ANALYSIS) {
            throw new BusinessRuleException("Somente modelos em análise podem ser removidos", "MODEL_NOT_REMOVABLE");
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
        if (!visible(model)) throw new UnauthorizedAccessException("Modelo indisponível neste contexto empresarial");
        return toResponse(model);
    }

    @Transactional(readOnly = true)
    public List<ModelResponseDTO> findAll() {
        return modelRepository.findAll().stream().filter(this::visible).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ModelResponseDTO> findByStatus(ModelStatus status) {
        return modelRepository.findByStatus(status).stream().filter(model -> visible(model) && (status != ModelStatus.APPROVED || rbac.isPlatformAdmin() || brands.canSelect(model.getBrandEntry(), rbac.getCurrentCompanyId()))).map(this::toResponse).toList();
    }

    private void validateProductFields(ModelRequestDTO dto) {
        ProductType productType = dto.getProductType() == null ? ProductType.PANEL : dto.getProductType();
        if (productType == ProductType.PANEL) {
            if (dto.getInfrastructureType() != null) {
                throw new InvalidFieldException("Tipo de infraestrutura não se aplica a placas");
            }
            if (dto.getType() == null || dto.getPowerWp() == null || dto.getEfficiency() == null
                    || dto.getWidth() == null || dto.getLength() == null || dto.getWeight() == null) {
                throw new InvalidFieldException("Placas exigem tipo, potência (Wp), eficiência, largura, comprimento e peso");
            }
            validateNumericFields(dto.getPowerWp(), dto.getEfficiency(), dto.getWidth(), dto.getLength(), dto.getWeight());
            return;
        }
        if (dto.getInfrastructureType() == null) {
            throw new InvalidFieldException("Produtos de infraestrutura exigem o tipo de infraestrutura");
        }
        if (dto.getType() != null || dto.getPowerWp() != null || dto.getEfficiency() != null) {
            throw new InvalidFieldException("Tipo de placa, potência e eficiência não se aplicam a produtos de infraestrutura");
        }
        for (BigDecimal dimension : new BigDecimal[] {dto.getWidth(), dto.getLength(), dto.getWeight()}) {
            if (dimension != null && dimension.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidFieldException("Dimensão ou peso inválido: " + dimension);
            }
        }
    }

    private void applyProductFields(Model model, ModelRequestDTO dto) {
        model.setModel(dto.getModel().strip());
        model.setProductType(dto.getProductType() == null ? ProductType.PANEL : dto.getProductType());
        model.setInfrastructureType(dto.getInfrastructureType());
        model.setMaterial(dto.getMaterial() == null || dto.getMaterial().isBlank() ? null : dto.getMaterial().strip());
        model.setType(dto.getType());
        model.setPowerWp(dto.getPowerWp());
        model.setEfficiency(dto.getEfficiency());
        model.setWidth(dto.getWidth());
        model.setLength(dto.getLength());
        model.setWeight(dto.getWeight());
    }

    private void validateNumericFields(BigDecimal powerWp, BigDecimal efficiency, BigDecimal width, BigDecimal length, BigDecimal weight) {
        if (powerWp.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Potência (Wp) inválida: " + powerWp);
        }
        if (efficiency.compareTo(BigDecimal.valueOf(100)) > 0) throw new InvalidFieldException("Eficiência deve ser menor ou igual a 100%");
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
        return ModelResponseDTO.builder().id(model.getId()).creatorCompanyId(model.getCreatorCompanyId()).brandId(model.getBrandId()).brand(model.getBrand()).model(model.getModel()).productType(model.getProductType()).infrastructureType(model.getInfrastructureType()).material(model.getMaterial()).type(model.getType()).powerWp(model.getPowerWp()).efficiency(model.getEfficiency()).width(model.getWidth()).length(model.getLength()).weight(model.getWeight()).status(model.getStatus()).build();
    }

    private boolean visible(Model model) {
        if (rbac.isPlatformAdmin()) return true;
        UUID companyId = rbac.getCurrentCompanyId();
        return brands.canSelect(model.getBrandEntry(), companyId) || java.util.Objects.equals(model.getCreatorCompanyId(), companyId);
    }

    private void requireOwner(Model model) {
        if (model.getCreatorCompanyId() == null) {
            throw new UnauthorizedAccessException("Modelo sem autoria registrada não permite alteração empresarial");
        }
        rbac.requireOwnCompany(model.getCreatorCompanyId());
    }
}
