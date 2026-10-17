package com.solaria.persistence.service.unit;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.entity.unit.LocalUnitPhoto;
import com.solaria.persistence.domain.enums.unit.LocalUnitPhotoType;
import com.solaria.persistence.domain.entity.execution.Requester;
import com.solaria.persistence.dto.request.unit.LocalUnitRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;
import com.solaria.persistence.dto.response.unit.LocalUnitResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
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
                            LocalUnitPhotoRepository localUnitPhotoRepository) {
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

        LocalUnit localUnit = new LocalUnit();
        localUnit.setRequester(requester);
        localUnit.setAddress(resolveAddress(dto.getAddressId()));
        localUnit.setComplement(dto.getComplement());
        localUnit.setLocationType(dto.getLocationType());

        return toResponse(localUnitRepository.save(localUnit));
    }

    @Transactional
    public LocalUnitResponseDTO update(UUID id, LocalUnitRequestDTO dto) {
        LocalUnit localUnit = localUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local com id:" + id + " não encontrada para atualização"));

        if (!dto.getRequesterId().equals(localUnit.getRequester().getId())) {
            throw new InvalidFieldException("requesterId imutável: " + dto.getRequesterId());
        }

        localUnit.setAddress(resolveAddress(dto.getAddressId()));
        localUnit.setComplement(dto.getComplement());
        localUnit.setLocationType(dto.getLocationType());

        return toResponse(localUnitRepository.save(localUnit));
    }

    @Transactional
    public LocalUnitResponseDTO attachAddress(UUID localUnitId, UUID addressId) {
        LocalUnit localUnit = localUnitRepository.findById(localUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + localUnitId));

        localUnit.setAddress(addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado com ID: " + addressId)));

        return toResponse(localUnitRepository.save(localUnit));
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!localUnitRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Unidade Local com id:" + id + " não encontrada para exclusão");
        }
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
        return toResponse(localUnit);
    }

    @Transactional(readOnly = true)
    public LocalUnitResponseDTO findById(UUID id, UUID companyId) {
        LocalUnit localUnit = localUnitRepository.findByIdAndRequester_Company_Id(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidade Local não encontrada com ID: " + id));
        return toResponse(localUnit);
    }

    @Transactional(readOnly = true)
    public List<LocalUnitResponseDTO> findByRequester(UUID requesterId) {
        return toResponses(localUnitRepository.findByRequesterId(requesterId));
    }

    @Transactional(readOnly = true)
    public List<LocalUnitResponseDTO> findAllByCompany(UUID companyId) {
        return toResponses(localUnitRepository.findByRequester_Company_Id(companyId));
    }

    private LocalUnitResponseDTO toResponse(LocalUnit entity) {
        return toResponse(entity, localUnitPhotoRepository.findByLocalUnitIdOrderByCreatedAtDesc(entity.getId()));
    }

    private List<LocalUnitResponseDTO> toResponses(List<LocalUnit> units) {
        if (units.isEmpty()) return List.of();
        Map<UUID, List<LocalUnitPhoto>> photos = localUnitPhotoRepository
                .findByLocalUnitIdInOrderByCreatedAtDesc(units.stream().map(LocalUnit::getId).toList()).stream()
                .collect(Collectors.groupingBy(photo -> photo.getLocalUnit().getId()));
        return units.stream().map(unit -> toResponse(unit, photos.getOrDefault(unit.getId(), List.of()))).toList();
    }

    private LocalUnitResponseDTO toResponse(LocalUnit entity, List<LocalUnitPhoto> photos) {
        LocalUnitResponseDTO response = new LocalUnitResponseDTO();
        response.setMainImageUrl(urlOf(photos, LocalUnitPhotoType.MAIN));
        response.setBannerUrl(urlOf(photos, LocalUnitPhotoType.BANNER));
        response.setGalleryUrls(photos.stream().filter(photo -> photo.getType() == LocalUnitPhotoType.GALLERY).map(LocalUnitPhoto::getUrl).toList());
        response.setId(entity.getId());
        response.setRequesterId(entity.getRequester().getId());
        response.setAddress(toAddressResponse(entity.getAddress()));
        response.setComplement(entity.getComplement());
        response.setLocationType(entity.getLocationType());
        return response;
    }

    private String urlOf(List<LocalUnitPhoto> photos, LocalUnitPhotoType type) {
        return photos.stream().filter(photo -> photo.getType() == type).map(LocalUnitPhoto::getUrl).findFirst().orElse(null);
    }

    private Address resolveAddress(UUID addressId) {
        if (addressId == null) {
            return null;
        }
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado com ID: " + addressId));
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
