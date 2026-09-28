package com.solaria.persistence.service.unit;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.dto.request.unit.LocalUnitRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;
import com.solaria.persistence.dto.response.unit.LocalUnitResponseDTO;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.dto.request.unit.LocalUnitUpdateRequestDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.shared.AddressRepository;
import com.solaria.persistence.repository.unit.EnergyBillRepository;
import com.solaria.persistence.repository.unit.LocalUnitPhotoRepository;
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.repository.proposal.ProposalUnitRepository;
import com.solaria.persistence.repository.execution.RequesterRepository;
import com.solaria.persistence.repository.unit.UnitSpecificationsRepository;


@Service
public class LocalUnitService {

    private final RbacAuthorizationService rbac;
    private final LocalUnitRepository localUnitRepository;
    private final RequesterRepository requesterRepository;
    private final AddressRepository addressRepository;
    private final UnitSpecificationsRepository unitSpecificationsRepository;
    private final EnergyBillRepository energyBillRepository;
    private final ProposalUnitRepository proposalUnitRepository;
    private final LocalUnitPhotoRepository localUnitPhotoRepository;

    public LocalUnitService(LocalUnitRepository localUnitRepository,
                            RequesterRepository requesterRepository,
                            AddressRepository addressRepository,
                            UnitSpecificationsRepository unitSpecificationsRepository,
                            EnergyBillRepository energyBillRepository,
                            ProposalUnitRepository proposalUnitRepository,
                            LocalUnitPhotoRepository localUnitPhotoRepository,
                            RbacAuthorizationService rbac) {
        this.rbac = rbac;
        this.localUnitRepository = localUnitRepository;
        this.requesterRepository = requesterRepository;
        this.addressRepository = addressRepository;
        this.unitSpecificationsRepository = unitSpecificationsRepository;
        this.energyBillRepository = energyBillRepository;
        this.proposalUnitRepository = proposalUnitRepository;
        this.localUnitPhotoRepository = localUnitPhotoRepository;
    }

    @Transactional
    public LocalUnitResponseDTO save(LocalUnitRequestDTO dto) {
        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitante não encontrado com ID: " + dto.getRequesterId()));

        rbac.requireOwnCompany(requester.getCompany().getId());
        LocalUnit localUnit = new LocalUnit();
        localUnit.setRequester(requester);
        localUnit.setAddress(resolveAddress(dto.getAddressId()));
        localUnit.setComplement(dto.getComplement());
        localUnit.setLocationType(dto.getLocationType());

        return toResponse(localUnitRepository.save(localUnit));
    }

    @Transactional
    public LocalUnitResponseDTO update(UUID id, LocalUnitUpdateRequestDTO dto) {
        LocalUnit localUnit = localUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local com id:" + id + " não encontrada para atualização"));

        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());

        localUnit.setAddress(resolveAddress(dto.getAddressId()));
        localUnit.setComplement(dto.getComplement());
        localUnit.setLocationType(dto.getLocationType());

        return toResponse(localUnitRepository.save(localUnit));
    }

    @Transactional
    public void deleteById(UUID id) {
        LocalUnit localUnit = localUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade Local não encontrada com ID: " + id));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());
        if (unitSpecificationsRepository.existsByLocalUnitId(id)
                || energyBillRepository.existsByLocalUnitId(id)
                || proposalUnitRepository.existsByLocalUnitId(id)
                || localUnitPhotoRepository.existsByLocalUnitId(id)) {
            throw new ResourceInUseException(
                    "Unidade Local não pode ser excluída: possui histórico/dependente(s) vinculado(s)");
        }
        localUnitRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public LocalUnitResponseDTO findById(UUID id) {
        LocalUnit localUnit = localUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + id));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());
        return toResponse(localUnit);
    }

    @Transactional(readOnly = true)
    public LocalUnitResponseDTO findById(UUID id, UUID companyId) {
        rbac.requireOwnCompany(companyId);
        LocalUnit localUnit = localUnitRepository.findByIdAndRequester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + id));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());
        return toResponse(localUnit);
    }

    @Transactional(readOnly = true)
    public List<LocalUnitResponseDTO> findByRequester(UUID requesterId) {
        Requester requester = requesterRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante não encontrado com ID: " + requesterId));
        rbac.requireOwnCompany(requester.getCompany().getId());
        return localUnitRepository.findByRequesterId(requesterId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LocalUnitResponseDTO> findAllByCompany(UUID companyId) {
        rbac.requireOwnCompany(companyId);
        return localUnitRepository.findByRequester_Company_Id(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private LocalUnitResponseDTO toResponse(LocalUnit entity) {
        LocalUnitResponseDTO response = new LocalUnitResponseDTO();
        response.setId(entity.getId());
        response.setRequesterId(entity.getRequester().getId());
        response.setAddress(toAddressResponse(entity.getAddress()));
        response.setComplement(entity.getComplement());
        response.setLocationType(entity.getLocationType());
        return response;
    }

    private Address resolveAddress(UUID addressId) {
        if (addressId == null) {
            return null;
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado com ID: " + addressId));
        rbac.requireOwnCompany(address.getOwnerCompany().getId());
        return address;
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
