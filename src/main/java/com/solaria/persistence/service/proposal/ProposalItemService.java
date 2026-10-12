package com.solaria.persistence.service.proposal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.proposal.ProposalItem;
import com.solaria.persistence.dto.response.catalog.ModelResponseDTO;
import com.solaria.persistence.dto.response.catalog.OfferResponseDTO;
import com.solaria.persistence.dto.response.proposal.ProposalItemResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.proposal.ProposalItemRepository;

@Service
public class ProposalItemService {

    private final ProposalItemRepository proposalItemRepository;
    private final ProposalService proposalService;

    public ProposalItemService(ProposalItemRepository proposalItemRepository, ProposalService proposalService) {
        this.proposalItemRepository = proposalItemRepository;
        this.proposalService = proposalService;
    }

    @Transactional(readOnly = true)
    public ProposalItemResponseDTO findById(UUID id) {
        return findParticipantItem(id);
    }

    private ProposalItemResponseDTO findParticipantItem(UUID id) {
        ProposalItem item = proposalItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de Proposta não encontrado com ID: " + id));
        proposalService.requireParticipant(item.getProposal());
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public ProposalItemResponseDTO findById(UUID id, UUID companyId) {
        proposalService.requireCompanyFilter(companyId);
        return findParticipantItem(id);
    }

    @Transactional(readOnly = true)
    public List<ProposalItemResponseDTO> findByProposal(UUID proposalId) {
        proposalService.findParticipant(proposalId);
        return proposalItemRepository.findByProposalIdWithOffer(proposalId).stream()
                .map(ProposalItemService::toResponse).toList();
    }

    static ProposalItemResponseDTO toResponse(ProposalItem entity) {
        ProposalItemResponseDTO response = new ProposalItemResponseDTO();
        response.setId(entity.getId());
        response.setProposalId(entity.getProposal().getId());
        response.setOffer(toOfferResponse(entity.getOffer()));
        response.setQuantity(entity.getQuantity());
        response.setNegotiatedPrice(entity.getNegotiatedPrice());
        response.setDiscount(entity.getDiscount());
        return response;
    }

    private static OfferResponseDTO toOfferResponse(Offer offer) {
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

    private static ModelResponseDTO toModelResponse(Model model) {
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
