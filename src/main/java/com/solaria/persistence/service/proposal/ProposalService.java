package com.solaria.persistence.service.proposal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import jakarta.persistence.EntityManager;
import com.solaria.persistence.exception.ProposalConflictException;
import com.solaria.persistence.repository.proposal.ProposalUnitRepository;
import com.solaria.persistence.dto.request.proposal.ProposalTermsRequestDTO;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.dto.request.proposal.ProposalCommercialItemRequestDTO;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.dto.request.proposal.ProposalRequestDTO;
import com.solaria.persistence.dto.response.proposal.ProposalResponseDTO;
import com.solaria.persistence.domain.entity.catalog.Inventory;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.proposal.Proposal;
import com.solaria.persistence.domain.entity.proposal.ProposalItem;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.domain.enums.proposal.ProposalStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.InventoryRepository;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.proposal.ProposalItemRepository;
import com.solaria.persistence.repository.proposal.ProposalRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final RequesterRepository requesterRepository;
    private final ProposalItemRepository proposalItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OfferRepository offerRepository;
    private final RbacAuthorizationService rbac;
    private final SupplierRepository supplierRepository;
    private final ProposalUnitRepository proposalUnitRepository;
    private final EntityManager entityManager;

    public ProposalService(ProposalRepository proposalRepository,
                           RequesterRepository requesterRepository,
                           ProposalItemRepository proposalItemRepository,
                           InventoryRepository inventoryRepository,
                           OfferRepository offerRepository,
                           RbacAuthorizationService rbac,
                           SupplierRepository supplierRepository,
                           ProposalUnitRepository proposalUnitRepository,
                           EntityManager entityManager) {
        this.proposalRepository = proposalRepository;
        this.requesterRepository = requesterRepository;
        this.proposalItemRepository = proposalItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.offerRepository = offerRepository;
        this.rbac = rbac;
        this.supplierRepository = supplierRepository;
        this.proposalUnitRepository = proposalUnitRepository;
        this.entityManager = entityManager;
    }

    public static boolean isEditable(ProposalStatus status) {
        return status == ProposalStatus.AWAITING_SUPPLIER || status == ProposalStatus.AWAITING_REQUESTER;
    }

    @Transactional
    public ProposalResponseDTO save(ProposalRequestDTO dto) {
        UUID companyId = rbac.getCurrentCompanyId();
        Requester requester = requesterRepository.findOneByCompanyId(companyId)
                .orElseThrow(() -> new UnauthorizedAccessException("Empresa sem solicitante"));
        if (dto.getSupplierId() == null) {
            throw new InvalidFieldException("supplierId é obrigatório");
        }
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Ofertante não encontrado"));
        validateSnapshot(dto.getItems(), dto.getNotes(), true);
        List<UUID> offerIds = dto.getItems().stream()
                .map(ProposalCommercialItemRequestDTO::getOfferId).toList();
        Map<UUID, Offer> offers = offerRepository.findByIdIn(offerIds).stream()
                .collect(Collectors.toMap(Offer::getId, offer -> offer));
        Proposal proposal = new Proposal();
        proposal.setRequester(requester);
        proposal.setSupplier(supplier);
        proposal.setStatus(ProposalStatus.AWAITING_SUPPLIER);
        proposal.setNotes(normalizeNotes(dto.getNotes()));
        proposal.setCreatedAt(OffsetDateTime.now());
        List<ProposalItem> items = new ArrayList<>();
        for (ProposalCommercialItemRequestDTO requested : dto.getItems()) {
            Offer offer = offers.get(requested.getOfferId());
            if (offer == null) {
                throw new ResourceNotFoundException("Oferta não encontrada: " + requested.getOfferId());
            }
            validateSupplier(proposal, offer);
            validateOfferAvailability(offer, requested.getQuantity());
            ProposalItem item = new ProposalItem();
            item.setProposal(proposal);
            item.setOffer(offer);
            item.setQuantity(requested.getQuantity());
            item.setNegotiatedPrice(requested.getNegotiatedPrice() == null
                    ? offer.getUnitPrice() : requested.getNegotiatedPrice());
            item.setDiscount(requested.getDiscount() == null ? BigDecimal.ZERO : requested.getDiscount());
            items.add(item);
        }
        computeTotal(proposal, items);
        Proposal saved = proposalRepository.save(proposal);
        List<ProposalItem> savedItems = proposalItemRepository.saveAll(items);
        return toResponse(saved, savedItems);
    }

    private void validateSnapshot(List<ProposalCommercialItemRequestDTO> items, String notes, boolean creation) {
        if (items == null || items.isEmpty()) throw new InvalidFieldException("items é obrigatório e não pode ser vazio");
        if (notes != null && notes.length() > 500) throw new InvalidFieldException("notes deve ter até 500 caracteres");
        Set<UUID> offerIds = new HashSet<>();
        for (ProposalCommercialItemRequestDTO item : items) {
            if (item == null || item.getOfferId() == null || creation && item.getId() != null) {
                throw new InvalidFieldException("Item inválido para envio");
            }
            validateSanity(item.getQuantity(), item.getNegotiatedPrice(), item.getDiscount());
            if (!offerIds.add(item.getOfferId())) throw new DuplicateResourceException("Oferta repetida: " + item.getOfferId());
        }
    }

    private static String normalizeNotes(String notes) { return notes == null || notes.isBlank() ? null : notes; }

    private static void validateSanity(Integer quantity, BigDecimal price, BigDecimal discount) {
        if (quantity == null || quantity <= 0) throw new InvalidFieldException("Quantidade deve ser positiva");
        if (price != null && price.signum() <= 0) throw new InvalidFieldException("Preço deve ser positivo");
        if (discount != null && (discount.signum() < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new InvalidFieldException("Desconto deve ser de 0 a 100");
        }
    }

    private static void validateSupplier(Proposal proposal, Offer offer) {
        if (!proposal.getSupplier().getId().equals(offer.getSupplier().getId())) {
            throw new InvalidFieldException("Oferta pertence a outro ofertante");
        }
    }

    private static void validateOfferAvailability(Offer offer, int quantity) {
        if (offer.getExpirationDate() != null && offer.getExpirationDate().isBefore(OffsetDateTime.now())) {
            throw new BusinessRuleException("Oferta expirada: " + offer.getId());
        }
        if (offer.getAvailability() < quantity) throw new BusinessRuleException("Offer sem disponibilidade suficiente");
    }

    @Transactional
    public ProposalResponseDTO updateTerms(UUID id, ProposalTermsRequestDTO dto) {
        Proposal proposal = lockParticipant(id);
        requireAwaitedParty(proposal);
        validateSnapshot(dto.getItems(), dto.getNotes(), false);
        List<ProposalItem> existing = proposalItemRepository.findByProposalIdWithOffer(id);
        existing.forEach(entityManager::refresh);
        Map<UUID, ProposalItem> byId = existing.stream().collect(Collectors.toMap(ProposalItem::getId, item -> item));
        Map<UUID, Long> allocated = allocatedQuantities(existing);
        Map<UUID, Offer> offers = offerRepository.findByIdIn(dto.getItems().stream()
                .map(ProposalCommercialItemRequestDTO::getOfferId).toList()).stream()
                .collect(Collectors.toMap(Offer::getId, offer -> offer));
        Set<UUID> retained = new HashSet<>();
        for (ProposalCommercialItemRequestDTO requested : dto.getItems()) {
            Offer offer = offers.get(requested.getOfferId());
            if (offer == null) throw new ResourceNotFoundException("Oferta não encontrada: " + requested.getOfferId());
            validateSupplier(proposal, offer);
            validateOfferAvailability(offer, requested.getQuantity());
            if (requested.getId() != null) {
                ProposalItem item = byId.get(requested.getId());
                if (item == null) throw new InvalidFieldException("Item não pertence à proposta");
                if (!retained.add(item.getId())) throw new DuplicateResourceException("Item repetido");
                if (!item.getOffer().getId().equals(requested.getOfferId())) {
                    throw new InvalidFieldException("Offer do item é imutável");
                }
                if (requested.getNegotiatedPrice() == null) {
                    throw new InvalidFieldException("Preço do item sobrevivente é obrigatório");
                }
                if (requested.getQuantity() < allocated.getOrDefault(item.getId(), 0L)) {
                    throw new BusinessRuleException("Quantidade menor que a soma alocada às unidades");
                }
            }
        }
        List<ProposalItem> removed = existing.stream().filter(item -> !retained.contains(item.getId())).toList();
        if (!removed.isEmpty()) {
            proposalUnitRepository.deleteAll(proposalUnitRepository
                    .findByProposalItemIdIn(removed.stream().map(ProposalItem::getId).toList()));
            proposalItemRepository.deleteAll(removed);
            proposalItemRepository.flush();
        }
        List<ProposalItem> reconciled = new ArrayList<>();
        for (ProposalCommercialItemRequestDTO requested : dto.getItems()) {
            ProposalItem item = requested.getId() == null ? new ProposalItem() : byId.get(requested.getId());
            item.setProposal(proposal);
            item.setOffer(offers.get(requested.getOfferId()));
            item.setQuantity(requested.getQuantity());
            item.setNegotiatedPrice(requested.getNegotiatedPrice() == null
                    ? item.getOffer().getUnitPrice() : requested.getNegotiatedPrice());
            item.setDiscount(requested.getDiscount() == null ? BigDecimal.ZERO : requested.getDiscount());
            reconciled.add(item);
        }
        proposal.setNotes(normalizeNotes(dto.getNotes()));
        computeTotal(proposal, reconciled);
        proposal.setStatus(proposal.getStatus() == ProposalStatus.AWAITING_SUPPLIER
                ? ProposalStatus.AWAITING_REQUESTER : ProposalStatus.AWAITING_SUPPLIER);
        proposalItemRepository.saveAll(reconciled);
        proposalRepository.save(proposal);
        return toResponse(proposal, reconciled);
    }

    private Map<UUID, Long> allocatedQuantities(List<ProposalItem> items) {
        if (items.isEmpty()) return Map.of();
        return proposalUnitRepository.sumQuantitiesByItemIds(items.stream().map(ProposalItem::getId).toList())
                .stream().collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).longValue()));
    }

    @Transactional
    public ProposalResponseDTO accept(UUID id) {
        Proposal proposal = lockParticipant(id);
        requireAwaitedParty(proposal);
        List<ProposalItem> items = proposalItemRepository.findByProposalIdWithOffer(id);
        items.forEach(entityManager::refresh);
        if (items.isEmpty()) throw new BusinessRuleException("Proposta não pode ser aceita sem itens");
        reserveInventory(proposal, items);
        computeTotal(proposal, items);
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalRepository.save(proposal);
        return toResponse(proposal, items);
    }

    @Transactional
    public ProposalResponseDTO cancel(UUID id) {
        Proposal proposal = lockParticipant(id);
        requireRequester(proposal);
        return close(proposal, ProposalStatus.CANCELED);
    }

    @Transactional
    public ProposalResponseDTO reject(UUID id) {
        Proposal proposal = lockParticipant(id);
        if (!rbac.getCurrentCompanyId().equals(proposal.getSupplier().getCompany().getId())) {
            throw new UnauthorizedAccessException("Somente Supplier pode recusar a proposta");
        }
        return close(proposal, ProposalStatus.REJECTED);
    }

    private ProposalResponseDTO close(Proposal proposal, ProposalStatus status) {
        requirePending(proposal);
        proposal.setStatus(status);
        proposal.setUpdatedAt(OffsetDateTime.now());
        proposalRepository.save(proposal);
        return toResponse(proposal);
    }

    Proposal lockParticipant(UUID id) {
        Proposal proposal = proposalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));
        entityManager.refresh(proposal);
        requireParticipant(proposal);
        return proposal;
    }

    void requireRequester(Proposal proposal) {
        if (!rbac.getCurrentCompanyId().equals(proposal.getRequester().getCompany().getId())) {
            throw new UnauthorizedAccessException("Somente Requester pode alterar este recurso");
        }
    }

    void requirePending(Proposal proposal) {
        if (!isEditable(proposal.getStatus())) throw new ProposalConflictException("Proposta em estado terminal");
    }

    private void requireAwaitedParty(Proposal proposal) {
        requirePending(proposal);
        UUID awaitedCompany = proposal.getStatus() == ProposalStatus.AWAITING_SUPPLIER
                ? proposal.getSupplier().getCompany().getId() : proposal.getRequester().getCompany().getId();
        if (!rbac.getCurrentCompanyId().equals(awaitedCompany)) {
            throw new ProposalConflictException("A proposta aguarda a outra parte");
        }
    }

    private void reserveInventory(Proposal proposal, List<ProposalItem> items) {
        Map<UUID, Long> required = items.stream().collect(Collectors.groupingBy(
                item -> item.getOffer().getModel().getId(), TreeMap::new,
                Collectors.summingLong(ProposalItem::getQuantity)));
        Map<UUID, Inventory> inventories = new TreeMap<>();
        for (Map.Entry<UUID, Long> entry : required.entrySet()) {
            Inventory inventory = inventoryRepository.findBySupplierIdAndModelIdForUpdate(
                    proposal.getSupplier().getId(), entry.getKey())
                    .orElseThrow(() -> new ProposalConflictException("Estoque não encontrado"));
            entityManager.refresh(inventory);
            if (inventory.getQuantity() < entry.getValue()) throw new ProposalConflictException("Estoque insuficiente");
            inventories.put(entry.getKey(), inventory);
        }
        for (Map.Entry<UUID, Inventory> entry : inventories.entrySet()) {
            Inventory inventory = entry.getValue();
            inventory.setQuantity(Math.toIntExact(inventory.getQuantity() - required.get(entry.getKey())));
            inventoryRepository.save(inventory);
        }
        OffsetDateTime now = OffsetDateTime.now();
        for (ProposalItem item : items.stream().sorted(java.util.Comparator.comparing(i -> i.getOffer().getId())).toList()) {
            Offer offer = item.getOffer();
            int remaining = inventories.get(offer.getModel().getId()).getQuantity();
            if (offerRepository.reserveAvailability(offer.getId(), item.getQuantity(), remaining, now) != 1) {
                throw new ProposalConflictException("Offer expirada ou sem disponibilidade corrente");
            }
            entityManager.refresh(offer);
        }
    }

    @Transactional(readOnly = true)
    public ProposalResponseDTO findById(UUID id) {
        Proposal proposal = findParticipant(id);
        return toResponse(proposal);
    }

    @Transactional(readOnly = true)
    public ProposalResponseDTO findById(UUID id, UUID companyId) {
        requireCompanyFilter(companyId);
        return findById(id);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponseDTO> findByRequester(UUID requesterId) {
        Requester requester = requesterRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante não encontrado"));
        UUID companyId = rbac.getCurrentCompanyId();
        List<Proposal> proposals = proposalRepository.findByRequesterAndParticipant(requesterId, companyId);
        if (proposals.isEmpty() && !companyId.equals(requester.getCompany().getId())) {
            throw new ResourceNotFoundException("Solicitante não encontrado");
        }
        return toResponses(proposals);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponseDTO> findAllByCompany(UUID companyId) {
        requireCompanyFilter(companyId);
        return toResponses(proposalRepository.findByParticipantCompany(companyId));
    }

    Proposal findParticipant(UUID id) {
        Proposal proposal = findEntity(id, "consulta");
        requireParticipant(proposal);
        return proposal;
    }

    void requireParticipant(Proposal proposal) {
        UUID companyId = rbac.getCurrentCompanyId();
        if (!companyId.equals(proposal.getRequester().getCompany().getId())
                && !companyId.equals(proposal.getSupplier().getCompany().getId())) {
            throw new ResourceNotFoundException("Proposta não encontrada");
        }
    }

    void requireCompanyFilter(UUID companyId) {
        if (!companyId.equals(rbac.getCurrentCompanyId())) {
            throw new ResourceNotFoundException("Proposta não encontrada");
        }
    }

    private List<ProposalResponseDTO> toResponses(List<Proposal> proposals) {
        if (proposals.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<ProposalItem>> items = proposalItemRepository
                .findByProposalIdIn(proposals.stream().map(Proposal::getId).toList()).stream()
                .collect(Collectors.groupingBy(item -> item.getProposal().getId()));
        return proposals.stream().map(proposal ->
                toResponse(proposal, items.getOrDefault(proposal.getId(), List.of()))).toList();
    }

    private Proposal computeTotal(Proposal proposal, List<ProposalItem> items) {

        BigDecimal total = BigDecimal.ZERO;
        for (ProposalItem item : items) {
            BigDecimal unitPrice = item.getNegotiatedPrice();
            BigDecimal discountPct = item.getDiscount();

            BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            BigDecimal discountFactor = BigDecimal.ONE.subtract(
                    discountPct.divide(BigDecimal.valueOf(100)));

            total = total.add(
                    gross.multiply(
                            discountFactor));
        }

        proposal.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        proposal.setUpdatedAt(OffsetDateTime.now());

        return proposal;
    }

    private Proposal findEntity(UUID id, String acao) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proposta não encontrada com ID: " + id + " para " + acao));
    }

    private ProposalResponseDTO toResponse(Proposal entity) {
        return toResponse(entity, proposalItemRepository.findByProposalIdWithOffer(entity.getId()));
    }

    private ProposalResponseDTO toResponse(Proposal entity, List<ProposalItem> items) {
        ProposalResponseDTO response = new ProposalResponseDTO();
        response.setId(entity.getId());
        response.setRequesterId(entity.getRequester().getId());
        response.setSupplierId(entity.getSupplier().getId());
        response.setStatus(entity.getStatus());
        response.setNotes(entity.getNotes());
        response.setTotalAmount(entity.getTotalAmount());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setItems(items.stream().map(ProposalItemService::toResponse).toList());
        return response;
    }
}
