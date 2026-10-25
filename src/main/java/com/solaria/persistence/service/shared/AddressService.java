package com.solaria.persistence.service.shared;

import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.dto.request.shared.AddressRequestDTO;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;
import com.solaria.persistence.exception.ResourceInUseException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.shared.AddressRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.shared.GeolocationRepository;
import com.solaria.persistence.repository.unit.LocalUnitRepository;

@Service
public class AddressService {

    private final RbacAuthorizationService rbac;
    private final AddressRepository addressRepository;
    private final CompanyRepository companyRepository;
    private final LocalUnitRepository localUnitRepository;
    private final GeolocationRepository geolocationRepository;

    public AddressService(AddressRepository addressRepository,
                          CompanyRepository companyRepository,
                          LocalUnitRepository localUnitRepository,
                          GeolocationRepository geolocationRepository,
                          RbacAuthorizationService rbac) {
        this.rbac = rbac;
        this.addressRepository = addressRepository;
        this.companyRepository = companyRepository;
        this.localUnitRepository = localUnitRepository;
        this.geolocationRepository = geolocationRepository;
    }

    @Transactional
    public AddressResponseDTO save(AddressRequestDTO dto) {
        Address address = new Address();
        address.setOwnerCompany(companyRepository.getReferenceById(rbac.getCurrentCompanyId()));
        address.setState(dto.getState());
        address.setCity(dto.getCity());
        address.setNeighborhood(dto.getNeighborhood());
        address.setZipCode(dto.getZipCode());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponseDTO update(UUID id, AddressRequestDTO dto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço com id:" + id + " não encontrado(a) para atualização"));

        rbac.requireOwnCompany(address.getOwnerCompany().getId());
        address.setState(dto.getState());
        address.setCity(dto.getCity());
        address.setNeighborhood(dto.getNeighborhood());
        address.setZipCode(dto.getZipCode());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteById(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado com ID: " + id));
        rbac.requireOwnCompany(address.getOwnerCompany().getId());
        if (companyRepository.existsByAddressId(id)
                || localUnitRepository.existsByAddressId(id)
                || geolocationRepository.existsByAddressId(id)) {
            throw new ResourceInUseException(
                    "Endereço não pode ser excluído(a): possui vínculo(s) em uso");
        }
        addressRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AddressResponseDTO findById(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado(a) com ID: " + id));
        rbac.requireOwnCompany(address.getOwnerCompany().getId());
        return toResponse(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> findAll() {
        return addressRepository.findByOwnerCompany_Id(rbac.getCurrentCompanyId()).stream().map(this::toResponse).toList();
    }

    private AddressResponseDTO toResponse(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId()).ownerCompanyId(address.getOwnerCompany().getId())
                .state(address.getState()).city(address.getCity()).neighborhood(address.getNeighborhood())
                .zipCode(address.getZipCode()).street(address.getStreet()).number(address.getNumber()).build();
    }
}
