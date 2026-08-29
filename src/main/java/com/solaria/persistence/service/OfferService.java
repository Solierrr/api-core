package com.solaria.persistence.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.Model;
import com.solaria.persistence.domain.entity.Offer;
import com.solaria.persistence.domain.entity.Supplier;
import com.solaria.persistence.domain.enums.ModelStatus;
import com.solaria.persistence.domain.enums.SupplierStatus;
import com.solaria.persistence.dto.request.OfferRequestDTO;
import com.solaria.persistence.dto.response.ModelResponseDTO;
import com.solaria.persistence.dto.response.OfferResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.ModelRepository;
import com.solaria.persistence.repository.OfferRepository;
import com.solaria.persistence.repository.ProposalItemRepository;
import com.solaria.persistence.repository.SupplierRepository;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final SupplierRepository supplierRepository;
    private final ModelRepository modelRepository;
    private final ProposalItemRepository proposalItemRepository;
    private final SubscriptionService subscriptionService;

    public OfferService(OfferRepository offerRepository,
                        SupplierRepository supplierRepository,
                        ModelRepository modelRepository,
                        ProposalItemRepository proposalItemRepository,
                        SubscriptionService subscriptionService) {
        this.offerRepository = offerRepository;
        this.supplierRepository = supplierRepository;
        this.modelRepository = modelRepository;
        this.proposalItemRepository = proposalItemRepository;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public OfferResponseDTO save(OfferRequestDTO dto) {
        validateUnitPrice(dto.getUnitPrice());
        if (dto.getAvailability() == null || dto.getAvailability() <= 0) {
            throw new InvalidFieldException("Disponibilidade inválida: " + dto.getAvailability());
        }
        validateExpirationDateInFuture(dto.getExpirationDate());

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fornecedor não encontrado com ID: " + dto.getSupplierId()));

        Model model = modelRepository.findById(dto.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Modelo não encontrado com ID: " + dto.getModelId()));

        validateSupplierAndSubscriptionGates(supplier);

        if (model.getStatus() != ModelStatus.APPROVED) {
            throw new BusinessRuleException("Modelo não está aprovado: " + model.getId());
        }

        Offer offer = new Offer();
        offer.setSupplier(supplier);
        offer.setModel(model);
        offer.setUnitPrice(dto.getUnitPrice());
        offer.setAvailability(dto.getAvailability());
        offer.setExpirationDate(dto.getExpirationDate());

        return toResponse(offerRepository.save(offer));
    }

    @Transactional
    public OfferResponseDTO update(UUID id, OfferRequestDTO dto) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oferta com id:" + id + " não encontrada para atualização"));

        if (dto.getSupplierId() != null && !dto.getSupplierId().equals(offer.getSupplier().getId())) {
            throw new InvalidFieldException("Fornecedor (supplierId) imutável: " + dto.getSupplierId());
        }
        if (dto.getModelId() != null && !dto.getModelId().equals(offer.getModel().getId())) {
            throw new InvalidFieldException("Modelo (modelId) imutável: " + dto.getModelId());
        }

        validateUnitPrice(dto.getUnitPrice());
        if (dto.getAvailability() == null || dto.getAvailability() < 0) {
            throw new InvalidFieldException("Disponibilidade inválida: " + dto.getAvailability());
        }
        validateExpirationDateInFuture(dto.getExpirationDate());

        validateSupplierAndSubscriptionGates(offer.getSupplier());

        offer.setUnitPrice(dto.getUnitPrice());
        offer.setAvailability(dto.getAvailability());
        offer.setExpirationDate(dto.getExpirationDate());

        return toResponse(offerRepository.save(offer));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!offerRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Oferta com id:" + id + " não encontrada para exclusão");
        }
        if (proposalItemRepository.existsByOfferId(id)) {
            throw new ResourceInUseException(
                    "Oferta não pode ser excluída: possui item(ns) de proposta vinculado(s)");
        }
        offerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public OfferResponseDTO findById(UUID id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oferta não encontrada com ID: " + id));
        return toResponse(offer);
    }

    @Transactional(readOnly = true)
    public OfferResponseDTO findById(UUID id, UUID companyId) {
        Offer offer = offerRepository.findByIdAndSupplier_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oferta não encontrada com ID: " + id));
        return toResponse(offer);
    }

    @Transactional(readOnly = true)
    public List<OfferResponseDTO> findBySupplier(UUID supplierId) {
        return offerRepository.findBySupplierId(supplierId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferResponseDTO> findAllByCompany(UUID companyId) {
        return offerRepository.findBySupplier_Company_Id(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferResponseDTO> findCatalog() {
        return offerRepository.findByExpirationDateIsNullOrExpirationDateAfter(OffsetDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Preço unitário inválido: " + unitPrice);
        }
    }

    private void validateExpirationDateInFuture(OffsetDateTime expirationDate) {
        if (expirationDate != null && !expirationDate.isAfter(OffsetDateTime.now())) {
            throw new InvalidFieldException("Data de expiração inválida: " + expirationDate);
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

    private OfferResponseDTO toResponse(Offer offer) {
        OfferResponseDTO response = new OfferResponseDTO();
        response.setId(offer.getId());
        response.setSupplierId(offer.getSupplier().getId());
        response.setModel(toModelResponse(offer.getModel()));
        response.setUnitPrice(offer.getUnitPrice());
        response.setAvailability(offer.getAvailability());
        response.setExpirationDate(offer.getExpirationDate());
        return response;
    }

    private ModelResponseDTO toModelResponse(Model model) {
        if (model == null) {
            return null;
        }
        return ModelResponseDTO.builder()
                .id(model.getId())
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
