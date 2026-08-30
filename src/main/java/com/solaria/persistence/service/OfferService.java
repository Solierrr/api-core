package com.solaria.persistence.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.solaria.persistence.domain.entity.Model;
import com.solaria.persistence.domain.entity.Offer;
import com.solaria.persistence.domain.entity.OfferTranslation;
import com.solaria.persistence.domain.entity.Supplier;
import com.solaria.persistence.domain.enums.ModelStatus;
import com.solaria.persistence.domain.enums.SupplierStatus;
import com.solaria.persistence.domain.enums.TranslationStatus;
import com.solaria.persistence.dto.request.OfferRequestDTO;
import com.solaria.persistence.dto.response.ModelResponseDTO;
import com.solaria.persistence.dto.response.OfferResponseDTO;
import com.solaria.persistence.dto.response.OfferTranslationResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.ModelRepository;
import com.solaria.persistence.repository.OfferRepository;
import com.solaria.persistence.repository.OfferTranslationRepository;
import com.solaria.persistence.repository.ProposalItemRepository;
import com.solaria.persistence.repository.SupplierRepository;
import com.solaria.persistence.util.SlugUtil;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final SupplierRepository supplierRepository;
    private final ModelRepository modelRepository;
    private final ProposalItemRepository proposalItemRepository;
    private final SubscriptionService subscriptionService;
    private final OfferTranslationRepository offerTranslationRepository;
    private final OfferTranslationService offerTranslationService;

    public OfferService(OfferRepository offerRepository,
                        SupplierRepository supplierRepository,
                        ModelRepository modelRepository,
                        ProposalItemRepository proposalItemRepository,
                        SubscriptionService subscriptionService,
                        OfferTranslationRepository offerTranslationRepository,
                        OfferTranslationService offerTranslationService) {
        this.offerRepository = offerRepository;
        this.supplierRepository = supplierRepository;
        this.modelRepository = modelRepository;
        this.proposalItemRepository = proposalItemRepository;
        this.subscriptionService = subscriptionService;
        this.offerTranslationRepository = offerTranslationRepository;
        this.offerTranslationService = offerTranslationService;
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
        offer.setDiscountPercentage(dto.getDiscountPercentage());
        offer.setServiceRegions(dto.getServiceRegions());
        offer.setSlug(generateUniqueSlug(model));
        offer.setTranslationStatus(TranslationStatus.PENDING);

        Offer savedOffer = offerRepository.save(offer);

        OfferTranslation pendingTranslation = new OfferTranslation();
        pendingTranslation.setOffer(savedOffer);
        pendingTranslation.setLocale(OfferTranslationService.PENDING_LOCALE);
        pendingTranslation.setTitle(dto.getTitle());
        pendingTranslation.setDescription(dto.getDescription());
        pendingTranslation.setDetails(dto.getDetails());
        offerTranslationRepository.save(pendingTranslation);

        triggerAsyncTranslationAfterCommit(savedOffer.getId());

        return toResponse(savedOffer);
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
        offer.setDiscountPercentage(dto.getDiscountPercentage());
        offer.setServiceRegions(dto.getServiceRegions());

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

    private String generateUniqueSlug(Model model) {
        String base = SlugUtil.slugify(model.getBrand() + " " + model.getModel());
        String candidate = base;
        int suffix = 2;
        while (offerRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    /**
     * Dispara a tradução em background somente após o commit da transação atual, garantindo
     * que a oferta e a tradução "pendente" já estejam visíveis para a thread assíncrona.
     */
    private void triggerAsyncTranslationAfterCommit(UUID offerId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            offerTranslationService.translateOffer(offerId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                offerTranslationService.translateOffer(offerId);
            }
        });
    }

    private OfferResponseDTO toResponse(Offer offer) {
        OfferResponseDTO response = new OfferResponseDTO();
        response.setId(offer.getId());
        response.setSupplierId(offer.getSupplier().getId());
        response.setModel(toModelResponse(offer.getModel()));
        response.setSlug(offer.getSlug());
        response.setUnitPrice(offer.getUnitPrice());
        response.setAvailability(offer.getAvailability());
        response.setExpirationDate(offer.getExpirationDate());
        response.setDiscountPercentage(offer.getDiscountPercentage());
        response.setServiceRegions(offer.getServiceRegions());
        response.setSourceLocale(offer.getSourceLocale());
        response.setTranslationStatus(offer.getTranslationStatus());
        response.setTranslations(offerTranslationRepository.findByOfferId(offer.getId()).stream()
                .filter(translation -> !OfferTranslationService.PENDING_LOCALE.equals(translation.getLocale()))
                .map(this::toTranslationResponse)
                .toList());
        return response;
    }

    private OfferTranslationResponseDTO toTranslationResponse(OfferTranslation translation) {
        OfferTranslationResponseDTO dto = new OfferTranslationResponseDTO();
        dto.setLocale(translation.getLocale());
        dto.setTitle(translation.getTitle());
        dto.setDescription(translation.getDescription());
        dto.setDetails(translation.getDetails());
        return dto;
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
