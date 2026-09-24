package com.solaria.persistence.service.proposal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public ProposalService(ProposalRepository proposalRepository,
                           RequesterRepository requesterRepository,
                           ProposalItemRepository proposalItemRepository,
                           InventoryRepository inventoryRepository,
                           OfferRepository offerRepository,
                           RbacAuthorizationService rbac,
                           ObjectMapper objectMapper) {
        this.proposalRepository = proposalRepository;
        this.requesterRepository = requesterRepository;
        this.proposalItemRepository = proposalItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.offerRepository = offerRepository;
        this.rbac = rbac;
        this.objectMapper = objectMapper;
    }

    public static boolean isEditable(ProposalStatus status) {
        return status == ProposalStatus.AWAITING_SUPPLIER || status == ProposalStatus.AWAITING_REQUESTER;
    }

    @Transactional
    public ProposalResponseDTO save(ProposalRequestDTO dto) {
        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitante não encontrado com ID: " + dto.getRequesterId()));

        Proposal proposal = new Proposal();
        proposal.setRequester(requester);
        proposal.setStatus(ProposalStatus.AWAITING_SUPPLIER);
        proposal.setNotes(dto.getNotes());
        proposal.setTotalAmount(null);
        proposal.setCreatedAt(OffsetDateTime.now());

        return toResponse(proposalRepository.save(proposal));
    }

    @Transactional
    public ProposalResponseDTO updateNotes(UUID id, String notes) {
        Proposal proposal = findEntity(id, "atualização");

        if (!isEditable(proposal.getStatus())) {
            throw new BusinessRuleException(
                    "Notas não podem ser alteradas: proposta em estado imutável (" + proposal.getStatus() + ")");
        }

        proposal.setNotes(notes);
        proposal.setUpdatedAt(OffsetDateTime.now());

        return toResponse(proposalRepository.save(proposal));
    }

    @Transactional
    public ProposalResponseDTO supplierAgree(UUID id) {
        return applyTransition(id, ProposalStatus.AWAITING_SUPPLIER, ProposalStatus.ACCEPTED,
                "aceite", "consentimento do ofertante");
    }

    @Transactional
    public ProposalResponseDTO supplierCounter(UUID id) {
        return applyTransition(id, ProposalStatus.AWAITING_SUPPLIER, ProposalStatus.AWAITING_REQUESTER,
                "contraproposta", "contraproposta do ofertante");
    }

    @Transactional
    public ProposalResponseDTO requesterAgree(UUID id) {
        return applyTransition(id, ProposalStatus.AWAITING_REQUESTER, ProposalStatus.ACCEPTED,
                "aceite", "consentimento do demandante");
    }

    @Transactional
    public ProposalResponseDTO requesterCounter(UUID id) {
        return applyTransition(id, ProposalStatus.AWAITING_REQUESTER, ProposalStatus.AWAITING_SUPPLIER,
                "contraproposta", "contraproposta do demandante");
    }

    @Transactional
    public ProposalResponseDTO reject(UUID id) {
        return applyEditableTransition(id, ProposalStatus.REJECTED, "recusa");
    }

    @Transactional
    public ProposalResponseDTO cancel(UUID id) {
        return applyEditableTransition(id, ProposalStatus.CANCELED, "cancelamento");
    }

    private ProposalResponseDTO applyTransition(UUID id, ProposalStatus expectedSource, ProposalStatus target,
                                                 String acaoBusca, String acaoTransicao) {
        Proposal proposal = findEntity(id, acaoBusca);
        rbac.requireOwnCompany(proposal.getRequester().getCompany().getId());
        requireStatus(proposal, expectedSource, acaoTransicao);

        Proposal saved = target == ProposalStatus.ACCEPTED
                ? finalizeAcceptance(proposal)
                : applyStatus(proposal, target);

        return toResponse(saved);
    }

    private Proposal applyStatus(Proposal proposal, ProposalStatus target) {
        proposal.setStatus(target);
        proposal.setUpdatedAt(OffsetDateTime.now());
        return proposalRepository.save(proposal);
    }

    private ProposalResponseDTO applyEditableTransition(UUID id, ProposalStatus target, String acao) {
        Proposal proposal = findEntity(id, acao);
        rbac.requireOwnCompany(proposal.getRequester().getCompany().getId());
        if (!isEditable(proposal.getStatus())) {
            throw new BusinessRuleException(
                    "Transição de status inválida: " + proposal.getStatus() + " → " + target);
        }
        return toResponse(applyStatus(proposal, target));
    }

    private Proposal finalizeAcceptance(Proposal proposal) {
        OffsetDateTime now = OffsetDateTime.now();
        List<ProposalItem> items = proposalItemRepository.findByProposalIdWithOffer(proposal.getId());

        if (items.isEmpty()) {
            throw new BusinessRuleException(
                    "Proposta " + proposal.getId() + " não pode ser aceita sem itens");
        }

        items.forEach(item -> reserveInventoryForItem(item, now));

        computeTotal(proposal);
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setUpdatedAt(now);

        return proposalRepository.save(proposal);
    }

    private void reserveInventoryForItem(ProposalItem item, OffsetDateTime now) {
        Offer offer = item.getOffer();

        boolean expired = offer.getExpirationDate() != null && offer.getExpirationDate().isBefore(now);
        if (expired) {
            throw new BusinessRuleException(
                    "Proposta não pode ser aceita: oferta " + offer.getId() + " expirada");
        }
        if (offer.getAvailability() < item.getQuantity()) {
            throw new BusinessRuleException(
                    "Proposta não pode ser aceita: oferta " + offer.getId() + " sem disponibilidade suficiente");
        }

        Inventory inventory = inventoryRepository
                .findBySupplierIdAndModelIdForUpdate(offer.getSupplier().getId(), offer.getModel().getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Estoque não encontrado para a oferta " + offer.getId()));

        if (inventory.getQuantity() < item.getQuantity()) {
            throw new BusinessRuleException(
                    "Estoque insuficiente para a oferta " + offer.getId()
                            + " (disponível: " + inventory.getQuantity() + ", solicitado: " + item.getQuantity() + ")");
        }

        inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
        inventoryRepository.save(inventory);

        int resynced = Math.min(offer.getAvailability() - item.getQuantity(), inventory.getQuantity());
        offer.setAvailability(Math.max(0, resynced));
        offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public ProposalResponseDTO findById(UUID id) {
        return toResponse(findEntity(id, "consulta"));
    }

    @Transactional(readOnly = true)
    public ProposalResponseDTO findById(UUID id, UUID companyId) {
        Proposal proposal = proposalRepository.findByIdAndRequester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proposta não encontrada com ID: " + id));
        return toResponse(proposal);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponseDTO> findByRequester(UUID requesterId) {
        return proposalRepository.findByRequesterId(requesterId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProposalResponseDTO> findAllByCompany(UUID companyId) {
        return proposalRepository.findByRequester_Company_Id(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Proposal recalculateTotal(Proposal proposal) {
        return computeTotal(proposal);
    }

    private Proposal computeTotal(Proposal proposal) {
        List<ProposalItem> items = proposalItemRepository.findByProposalId(proposal.getId());

        BigDecimal total = BigDecimal.ZERO;
        for (ProposalItem item : items) {
            BigDecimal unitPrice = item.getNegotiatedPrice() != null
                    ? item.getNegotiatedPrice()
                    : item.getOffer().getUnitPrice();
            BigDecimal discountPct = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;

            BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            BigDecimal discountFactor = BigDecimal.ONE.subtract(
                    discountPct.divide(
                            BigDecimal.valueOf(100),
                            6,
                            RoundingMode.HALF_UP));

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

    private void requireStatus(Proposal proposal, ProposalStatus expected, String acao) {
        if (proposal.getStatus() != expected) {
            throw new BusinessRuleException(
                    acao + " inválido(a): proposta em status " + proposal.getStatus() + " (esperado: " + expected + ")");
        }
    }

    private ProposalResponseDTO toResponse(Proposal entity) {
        ProposalResponseDTO response = objectMapper.convertValue(entity, ProposalResponseDTO.class);
        response.setRequesterId(entity.getRequester().getId());
        return response;
    }
}
