package com.solaria.persistence.service.shared;

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
import com.solaria.persistence.repository.shared.GeolocalizationRepository;
import com.solaria.persistence.repository.unit.LocalUnitRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CompanyRepository companyRepository;
    private final LocalUnitRepository localUnitRepository;
    private final GeolocalizationRepository geolocalizationRepository;
    private final ObjectMapper objectMapper;

    public AddressService(AddressRepository addressRepository,
                          CompanyRepository companyRepository,
                          LocalUnitRepository localUnitRepository,
                          GeolocalizationRepository geolocalizationRepository,
                          ObjectMapper objectMapper) {
        this.addressRepository = addressRepository;
        this.companyRepository = companyRepository;
        this.localUnitRepository = localUnitRepository;
        this.geolocalizationRepository = geolocalizationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AddressResponseDTO save(AddressRequestDTO dto) {
        Address address = new Address();
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
        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Endereço com id:" + id + " não encontrado(a) para exclusão");
        }
        if (companyRepository.existsByAddressId(id)
                || localUnitRepository.existsByAddressId(id)
                || geolocalizationRepository.existsByAddressId(id)) {
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
        return toResponse(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> findAll() {
        return addressRepository.findAll().stream().map(this::toResponse).toList();
    }

    private AddressResponseDTO toResponse(Address address) {
        return objectMapper.convertValue(address, AddressResponseDTO.class);
    }
}
