package com.solaria.persistence.service.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.catalog.Inventory;
import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.enums.catalog.SupplierStatus;
import com.solaria.persistence.dto.request.catalog.InventoryRequestDTO;
import com.solaria.persistence.dto.response.catalog.InventoryResponseDTO;
import com.solaria.persistence.dto.response.catalog.ModelResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.InventoryRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.catalog.ModelRepository;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.service.company.SubscriptionService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
public class InventoryService {

    private final RbacAuthorizationService rbac;
    private final CompanyRepository companyRepository;

    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final ModelRepository modelRepository;
    private final SubscriptionService subscriptionService;

    public InventoryService(InventoryRepository inventoryRepository,
                            SupplierRepository supplierRepository,
                            ModelRepository modelRepository,
                            SubscriptionService subscriptionService,
                            RbacAuthorizationService rbac,
                            CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
        this.rbac = rbac;
        this.inventoryRepository = inventoryRepository;
        this.supplierRepository = supplierRepository;
        this.modelRepository = modelRepository;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public InventoryResponseDTO save(InventoryRequestDTO dto) {
        validateQuantity(dto.getQuantity());

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fornecedor não encontrado com ID: " + dto.getSupplierId()));

        rbac.requireOwnCompany(supplier.getCompany().getId());

        Model model = modelRepository.findById(dto.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Modelo não encontrado com ID: " + dto.getModelId()));

        if (inventoryRepository.existsBySupplierIdAndModelId(dto.getSupplierId(), dto.getModelId())) {
            throw new DuplicateResourceException(
                    "Estoque já cadastrado para o fornecedor " + dto.getSupplierId() + " e modelo " + dto.getModelId());
        }

        validateSupplierAndSubscriptionGates(supplier);

        Inventory inventory = new Inventory();
        inventory.setSupplier(supplier);
        inventory.setModel(model);
        inventory.setQuantity(dto.getQuantity());

        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponseDTO updateQuantity(UUID id, Integer quantity) {
        validateQuantity(quantity);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estoque com id:" + id + " não encontrado para atualização"));

        rbac.requireOwnCompany(inventory.getSupplier().getCompany().getId());

        validateSupplierAndSubscriptionGates(inventory.getSupplier());

        inventory.setQuantity(quantity);
        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public void deleteById(UUID id) {
        Inventory resource = inventoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                    "Estoque com id:" + id + " não encontrado para exclusão"));
        rbac.requireOwnCompany(resource.getSupplier().getCompany().getId());
        inventoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO findById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estoque não encontrado com ID: " + id));
        rbac.requireOwnCompany(inventory.getSupplier().getCompany().getId());
        return toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO findById(UUID id, UUID companyId) {
        requireCompany(companyId);
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estoque não encontrado com ID: " + id));
        rbac.requireOwnCompany(inventory.getSupplier().getCompany().getId());
        return toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findBySupplier(UUID supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(
                () -> new ResourceNotFoundException("Fornecedor não encontrado com ID: " + supplierId));
        rbac.requireOwnCompany(supplier.getCompany().getId());
        return inventoryRepository.findBySupplierId(supplierId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findAllByCompany(UUID companyId) {
        requireCompany(companyId);
        return inventoryRepository.findBySupplier_Company_Id(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void requireCompany(UUID companyId) {
        var company = companyRepository.findById(companyId).orElseThrow(
                () -> new ResourceNotFoundException("Empresa inexistente: " + companyId));
        rbac.requireOwnCompany(company.getId());
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new InvalidFieldException("Quantidade inválida: " + quantity);
        }
    }

    private void validateSupplierAndSubscriptionGates(Supplier supplier) {
        if (supplier.getStatus() != SupplierStatus.ACTIVE) {
            throw new BusinessRuleException("Fornecedor não está ativo: " + supplier.getId());
        }
        if (!subscriptionService.isSupplierSubscriptionActive(supplier.getId())) {
            throw new BusinessRuleException("Fornecedor não possui assinatura ativa: " + supplier.getId());
        }
    }

    private InventoryResponseDTO toResponse(Inventory inventory) {
        InventoryResponseDTO response = new InventoryResponseDTO();
        response.setId(inventory.getId());
        response.setSupplierId(inventory.getSupplier().getId());
        response.setModel(toModelResponse(inventory.getModel()));
        response.setQuantity(inventory.getQuantity());
        return response;
    }

    private ModelResponseDTO toModelResponse(Model model) {
        if (model == null) {
            return null;
        }
        return ModelResponseDTO.builder()
                .id(model.getId())
                .creatorCompanyId(model.getCreatorCompanyId())
                .brand(model.getBrand())
                .model(model.getModel())
                .type(model.getType())
                .powerWp(model.getPowerWp())
                .efficiency(model.getEfficiency())
                .width(model.getWidth())
                .length(model.getLength())
                .weight(model.getWeight())
                .status(model.getStatus())
                .build();
    }
}
