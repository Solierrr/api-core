package com.solaria.persistence.service.proposal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Address;
import jakarta.persistence.EntityManager;
import com.solaria.persistence.domain.entity.proposal.Proposal;
import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.entity.proposal.ProposalItem;
import com.solaria.persistence.domain.entity.proposal.ProposalUnit;
import com.solaria.persistence.dto.request.proposal.ProposalUnitRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;
import com.solaria.persistence.dto.response.unit.LocalUnitResponseDTO;
import com.solaria.persistence.dto.response.proposal.ProposalUnitResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.repository.proposal.ProposalItemRepository;
import com.solaria.persistence.repository.proposal.ProposalUnitRepository;

@Service
public class ProposalUnitService {

    private final ProposalUnitRepository proposalUnitRepository;
    private final ProposalItemRepository proposalItemRepository;
    private final LocalUnitRepository localUnitRepository;
    private final ProposalService proposalService;
    private final EntityManager entityManager;

    public ProposalUnitService(ProposalUnitRepository proposalUnitRepository,
                               ProposalItemRepository proposalItemRepository,
                               LocalUnitRepository localUnitRepository,
                               ProposalService proposalService, EntityManager entityManager) {
        this.proposalUnitRepository = proposalUnitRepository;
        this.proposalItemRepository = proposalItemRepository;
        this.localUnitRepository = localUnitRepository;
        this.proposalService = proposalService;
        this.entityManager = entityManager;
    }

    @Transactional
    public ProposalUnitResponseDTO save(ProposalUnitRequestDTO dto) {
        validateQuantity(dto.getQuantity());

        UUID proposalId = proposalItemRepository.findProposalIdById(dto.getProposalItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item da proposta não encontrado"));
        Proposal parent = proposalService.lockParticipant(proposalId);
        proposalService.requireRequester(parent);
        proposalService.requirePending(parent);
        ProposalItem proposalItem = proposalItemRepository.findById(dto.getProposalItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item da Proposta não encontrado com ID: " + dto.getProposalItemId()));

        entityManager.refresh(proposalItem);

        LocalUnit localUnit = localUnitRepository.findById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + dto.getLocalUnitId()));

        validateScopeCoherence(localUnit, proposalItem);

        long currentSum = sumOrZero(proposalUnitRepository.sumQuantityByProposalItemId(dto.getProposalItemId()));
        if (currentSum + dto.getQuantity() > proposalItem.getQuantity()) {
            throw new InvalidFieldException("Quantidade excede o item de proposta");
        }

        ProposalUnit proposalUnit = new ProposalUnit();
        proposalUnit.setProposalItem(proposalItem);
        proposalUnit.setLocalUnit(localUnit);
        proposalUnit.setQuantity(dto.getQuantity());
        proposalUnit.setNote(dto.getNote());

        return toResponse(proposalUnitRepository.save(proposalUnit));
    }

    @Transactional
    public ProposalUnitResponseDTO update(UUID id, ProposalUnitRequestDTO dto) {
        ProposalUnit proposalUnit = findLockedUnit(id);

        if (!dto.getProposalItemId().equals(proposalUnit.getProposalItem().getId())) {
            throw new InvalidFieldException("proposalItemId imutável: " + dto.getProposalItemId());
        }

        validateQuantity(dto.getQuantity());

        LocalUnit localUnit = localUnitRepository.findById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + dto.getLocalUnitId()));

        ProposalItem proposalItem = proposalUnit.getProposalItem();
        validateScopeCoherence(localUnit, proposalItem);

        long currentSum = sumOrZero(proposalUnitRepository.sumQuantityByProposalItemId(proposalItem.getId()));
        long sumExcludingCurrent = currentSum - proposalUnit.getQuantity();
        if (sumExcludingCurrent + dto.getQuantity() > proposalItem.getQuantity()) {
            throw new InvalidFieldException("Quantidade excede o item de proposta");
        }

        proposalUnit.setLocalUnit(localUnit);
        proposalUnit.setQuantity(dto.getQuantity());
        proposalUnit.setNote(dto.getNote());

        return toResponse(proposalUnitRepository.save(proposalUnit));
    }

    @Transactional
    public void deleteById(UUID id) {
        findLockedUnit(id);
        proposalUnitRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProposalUnitResponseDTO findById(UUID id) {
        return findParticipantUnit(id);
    }

    private ProposalUnitResponseDTO findParticipantUnit(UUID id) {
        ProposalUnit proposalUnit = proposalUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade da Proposta não encontrada com ID: " + id));
        proposalService.requireParticipant(proposalUnit.getProposalItem().getProposal());
        return toResponse(proposalUnit);
    }

    @Transactional(readOnly = true)
    public ProposalUnitResponseDTO findById(UUID id, UUID companyId) {
        proposalService.requireCompanyFilter(companyId);
        return findParticipantUnit(id);
    }

    @Transactional(readOnly = true)
    public List<ProposalUnitResponseDTO> findByProposalItem(UUID proposalItemId) {
        ProposalItem item = proposalItemRepository.findById(proposalItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item da proposta não encontrado"));
        proposalService.requireParticipant(item.getProposal());
        return proposalUnitRepository.findByProposalItemId(proposalItemId).stream()
                .map(this::toResponse).toList();
    }

    private ProposalUnit findLockedUnit(UUID id) {
        UUID proposalId = proposalUnitRepository.findProposalIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade da proposta não encontrada"));
        Proposal parent = proposalService.lockParticipant(proposalId);
        proposalService.requireRequester(parent);
        proposalService.requirePending(parent);
        ProposalUnit unit = proposalUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade da proposta não encontrada"));
        entityManager.refresh(unit);
        entityManager.refresh(unit.getProposalItem());
        return unit;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidFieldException("Quantidade inválida: " + quantity);
        }
    }

    private void validateScopeCoherence(LocalUnit localUnit, ProposalItem proposalItem) {
        UUID localUnitRequesterId = localUnit.getRequester().getId();
        UUID proposalRequesterId = proposalItem.getProposal().getRequester().getId();
        if (!localUnitRequesterId.equals(proposalRequesterId)) {
            throw new UnauthorizedAccessException("Unidade pertence a outro demandante");
        }
    }

    private long sumOrZero(Long sum) {
        return sum == null ? 0 : sum;
    }

    private ProposalUnitResponseDTO toResponse(ProposalUnit entity) {
        ProposalUnitResponseDTO response = new ProposalUnitResponseDTO();
        response.setId(entity.getId());
        response.setProposalItemId(entity.getProposalItem().getId());
        response.setLocalUnit(toLocalUnitResponse(entity.getLocalUnit()));
        response.setQuantity(entity.getQuantity());
        response.setNote(entity.getNote());
        return response;
    }

    private LocalUnitResponseDTO toLocalUnitResponse(LocalUnit entity) {
        if (entity == null) {
            return null;
        }
        LocalUnitResponseDTO response = new LocalUnitResponseDTO();
        response.setId(entity.getId());
        response.setRequesterId(entity.getRequester().getId());
        response.setAddress(toAddressResponse(entity.getAddress()));
        response.setComplement(entity.getComplement());
        response.setLocationType(entity.getLocationType());
        return response;
    }

    private AddressResponseDTO toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponseDTO.builder()
                .id(address.getId())
                .ownerCompanyId(address.getOwnerCompany().getId())
                .state(address.getState())
                .city(address.getCity())
                .neighborhood(address.getNeighborhood())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .number(address.getNumber())
                .build();
    }
}
