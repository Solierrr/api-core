package com.solaria.persistence.service.proposal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.entity.proposal.ProposalItem;
import com.solaria.persistence.domain.entity.proposal.ProposalUnit;
import com.solaria.persistence.domain.enums.proposal.ProposalStatus;
import com.solaria.persistence.dto.request.proposal.ProposalUnitRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;
import com.solaria.persistence.dto.response.unit.LocalUnitResponseDTO;
import com.solaria.persistence.dto.response.proposal.ProposalUnitResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
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

    public ProposalUnitService(ProposalUnitRepository proposalUnitRepository,
                               ProposalItemRepository proposalItemRepository,
                               LocalUnitRepository localUnitRepository) {
        this.proposalUnitRepository = proposalUnitRepository;
        this.proposalItemRepository = proposalItemRepository;
        this.localUnitRepository = localUnitRepository;
    }

    @Transactional
    public ProposalUnitResponseDTO save(ProposalUnitRequestDTO dto) {
        validateQuantity(dto.getQuantity());

        ProposalItem proposalItem = proposalItemRepository.findById(dto.getProposalItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item da Proposta não encontrado com ID: " + dto.getProposalItemId()));

        LocalUnit localUnit = localUnitRepository.findById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + dto.getLocalUnitId()));

        validateScopeCoherence(localUnit, proposalItem);
        validateProposalMutableState(proposalItem);

        Integer currentSum = sumOrZero(proposalUnitRepository.sumQuantityByProposalItemId(dto.getProposalItemId()));
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
        ProposalUnit proposalUnit = proposalUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade da Proposta com id:" + id + " não encontrada para atualização"));

        if (!dto.getProposalItemId().equals(proposalUnit.getProposalItem().getId())) {
            throw new InvalidFieldException("proposalItemId imutável: " + dto.getProposalItemId());
        }

        validateQuantity(dto.getQuantity());

        LocalUnit localUnit = localUnitRepository.findById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + dto.getLocalUnitId()));

        ProposalItem proposalItem = proposalUnit.getProposalItem();
        validateScopeCoherence(localUnit, proposalItem);
        validateProposalMutableState(proposalItem);

        Integer currentSum = sumOrZero(proposalUnitRepository.sumQuantityByProposalItemId(proposalItem.getId()));
        int sumExcludingCurrent = currentSum - proposalUnit.getQuantity();
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
        ProposalUnit proposalUnit = proposalUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade da Proposta com id:" + id + " não encontrada para exclusão"));

        validateProposalMutableState(proposalUnit.getProposalItem());

        proposalUnitRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProposalUnitResponseDTO findById(UUID id) {
        ProposalUnit proposalUnit = proposalUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade da Proposta não encontrada com ID: " + id));
        return toResponse(proposalUnit);
    }

    @Transactional(readOnly = true)
    public ProposalUnitResponseDTO findById(UUID id, UUID companyId) {
        ProposalUnit proposalUnit = proposalUnitRepository
                .findByIdAndProposalItem_Proposal_Requester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade da Proposta não encontrada com ID: " + id));
        return toResponse(proposalUnit);
    }

    @Transactional(readOnly = true)
    public List<ProposalUnitResponseDTO> findByProposalItem(UUID proposalItemId) {
        return proposalUnitRepository.findByProposalItemId(proposalItemId).stream()
                .map(this::toResponse)
                .toList();
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

    private void validateProposalMutableState(ProposalItem proposalItem) {
        ProposalStatus status = proposalItem.getProposal().getStatus();
        if (!ProposalService.isEditable(status)) {
            throw new BusinessRuleException("Proposta não pode ser alterada no status: " + status);
        }
    }

    private Integer sumOrZero(Integer sum) {
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
                .state(address.getState())
                .city(address.getCity())
                .neighborhood(address.getNeighborhood())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .number(address.getNumber())
                .build();
    }
}
