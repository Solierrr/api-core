package com.solaria.persistence.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.Model;
import com.solaria.persistence.domain.entity.Offer;
import com.solaria.persistence.domain.entity.Proposal;
import com.solaria.persistence.domain.entity.ProposalItem;
import com.solaria.persistence.dto.request.ProposalItemRequestDTO;
import com.solaria.persistence.dto.response.ModelResponseDTO;
import com.solaria.persistence.dto.response.OfferResponseDTO;
import com.solaria.persistence.dto.response.ProposalItemResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.OfferRepository;
import com.solaria.persistence.repository.ProposalItemRepository;
import com.solaria.persistence.repository.ProposalRepository;

@Service
public class ProposalItemService {

    private final ProposalItemRepository proposalItemRepository;
    private final ProposalRepository proposalRepository;
    private final OfferRepository offerRepository;
    private final ProposalService proposalService;

    public ProposalItemService(ProposalItemRepository proposalItemRepository,
                               ProposalRepository proposalRepository,
                               OfferRepository offerRepository,
                               ProposalService proposalService) {
        this.proposalItemRepository = proposalItemRepository;
        this.proposalRepository = proposalRepository;
        this.offerRepository = offerRepository;
        this.proposalService = proposalService;
    }

    @Transactional
    public ProposalItemResponseDTO save(ProposalItemRequestDTO dto) {
        validateSanity(dto.getQuantity(), dto.getNegotiatedPrice(), dto.getDiscount());

        Proposal proposal = proposalRepository.findById(dto.getProposalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proposta não encontrada com ID: " + dto.getProposalId()));

        Offer offer = offerRepository.findById(dto.getOfferId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oferta não encontrada com ID: " + dto.getOfferId()));

        if (proposalItemRepository.existsByProposalIdAndOfferId(dto.getProposalId(), dto.getOfferId())) {
            throw new DuplicateResourceException(
                    "Oferta " + dto.getOfferId() + " já vinculada à proposta " + dto.getProposalId());
        }

        validateProposalMutable(proposal);
        validateOfferAvailability(offer, dto.getQuantity());

        ProposalItem item = new ProposalItem();
        item.setProposal(proposal);
        item.setOffer(offer);
        item.setQuantity(dto.getQuantity());
        item.setNegotiatedPrice(dto.getNegotiatedPrice());
        item.setDiscount(dto.getDiscount());

        ProposalItem saved = proposalItemRepository.save(item);

        proposalService.recalculateTotal(proposal);
        proposalRepository.save(proposal);

        return toResponse(saved);
    }

    @Transactional
    public ProposalItemResponseDTO update(UUID id, ProposalItemRequestDTO dto) {
        ProposalItem item = proposalItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de Proposta com id:" + id + " não encontrado para atualização"));

        if (!dto.getProposalId().equals(item.getProposal().getId())) {
            throw new InvalidFieldException("proposalId imutável: " + dto.getProposalId());
        }
        if (!dto.getOfferId().equals(item.getOffer().getId())) {
            throw new InvalidFieldException("offerId imutável: " + dto.getOfferId());
        }

        validateSanity(dto.getQuantity(), dto.getNegotiatedPrice(), dto.getDiscount());

        Proposal proposal = item.getProposal();
        Offer offer = item.getOffer();

        validateProposalMutable(proposal);
        validateOfferAvailability(offer, dto.getQuantity());

        item.setQuantity(dto.getQuantity());
        item.setNegotiatedPrice(dto.getNegotiatedPrice());
        item.setDiscount(dto.getDiscount());

        ProposalItem saved = proposalItemRepository.save(item);

        proposalService.recalculateTotal(proposal);
        proposalRepository.save(proposal);

        return toResponse(saved);
    }

    @Transactional
    public void deleteById(UUID id) {
        ProposalItem item = proposalItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de Proposta com id:" + id + " não encontrado para exclusão"));

        Proposal proposal = item.getProposal();
        validateProposalMutable(proposal);

        proposalItemRepository.deleteById(id);

        proposalService.recalculateTotal(proposal);
        proposalRepository.save(proposal);
    }

    @Transactional(readOnly = true)
    public ProposalItemResponseDTO findById(UUID id) {
        ProposalItem item = proposalItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de Proposta não encontrado com ID: " + id));
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public ProposalItemResponseDTO findById(UUID id, UUID companyId) {
        ProposalItem item = proposalItemRepository.findByIdAndProposal_Requester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de Proposta não encontrado com ID: " + id));
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public List<ProposalItemResponseDTO> findByProposal(UUID proposalId) {
        return proposalItemRepository.findByProposalId(proposalId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateSanity(Integer quantity, BigDecimal negotiatedPrice, BigDecimal discount) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidFieldException("Quantidade inválida: " + quantity);
        }
        if (negotiatedPrice != null && negotiatedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFieldException("Preço negociado inválido: " + negotiatedPrice);
        }
        if (discount != null
                && (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new InvalidFieldException("Desconto percentual inválido (esperado 0-100): " + discount);
        }
    }

    private void validateProposalMutable(Proposal proposal) {
        if (!ProposalService.isEditable(proposal.getStatus())) {
            throw new BusinessRuleException(
                    "Item de Proposta não pode ser alterado: proposta em estado imutável (" + proposal.getStatus() + ")");
        }
    }

    private void validateOfferAvailability(Offer offer, Integer quantity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (offer.getExpirationDate() != null && offer.getExpirationDate().isBefore(now)) {
            throw new BusinessRuleException("Oferta expirada: " + offer.getId());
        }
        if (offer.getAvailability() < quantity) {
            throw new BusinessRuleException(
                    "Oferta " + offer.getId() + " sem disponibilidade suficiente para a quantidade solicitada");
        }
    }

    private ProposalItemResponseDTO toResponse(ProposalItem entity) {
        ProposalItemResponseDTO response = new ProposalItemResponseDTO();
        response.setId(entity.getId());
        response.setProposalId(entity.getProposal().getId());
        response.setOffer(toOfferResponse(entity.getOffer()));
        response.setQuantity(entity.getQuantity());
        response.setNegotiatedPrice(entity.getNegotiatedPrice());
        response.setDiscount(entity.getDiscount());
        return response;
    }

    private OfferResponseDTO toOfferResponse(Offer offer) {
        if (offer == null) {
            return null;
        }
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
