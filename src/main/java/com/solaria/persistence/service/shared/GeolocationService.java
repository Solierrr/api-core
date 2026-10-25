package com.solaria.persistence.service.shared;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.shared.Geolocation;
import com.solaria.persistence.dto.request.shared.GeolocationRequestDTO;
import com.solaria.persistence.dto.response.shared.GeolocationResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.shared.AddressRepository;
import com.solaria.persistence.repository.shared.GeolocationRepository;

@Service
public class GeolocationService {

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");

    private final GeolocationRepository geolocationRepository;
    private final AddressRepository addressRepository;
    private final RbacAuthorizationService rbac;

    public GeolocationService(GeolocationRepository geolocationRepository,
                                  AddressRepository addressRepository,
                                  RbacAuthorizationService rbac) {
        this.rbac = rbac;
        this.geolocationRepository = geolocationRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public GeolocationResponseDTO save(GeolocationRequestDTO dto) {
        validateRequiredFields(dto);
        validateCoordinateRanges(dto.getLatitude(), dto.getLongitude());

        Address address = resolveAddress(dto.getAddressId());
        if (geolocationRepository.existsByAddressId(dto.getAddressId())) {
            throw new DuplicateResourceException(
                    "Geolocalização já cadastrada para o endereço: " + dto.getAddressId());
        }

        Geolocation geolocation = new Geolocation();
        geolocation.setAddress(address);
        geolocation.setLatitude(dto.getLatitude());
        geolocation.setLongitude(dto.getLongitude());

        return toResponse(geolocationRepository.save(geolocation));
    }

    @Transactional
    public GeolocationResponseDTO update(UUID id, GeolocationRequestDTO dto) {
        Geolocation geolocation = geolocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geolocalização com id:" + id + " não encontrada para atualização"));

        validateRequiredFields(dto);
        validateCoordinateRanges(dto.getLatitude(), dto.getLongitude());

        Address currentAddress = geolocation.getAddress();
        rbac.requireOwnCompany(currentAddress.getOwnerCompany().getId());
        Address address = resolveAddress(dto.getAddressId());
        boolean addressChanged = !dto.getAddressId().equals(currentAddress.getId());

        if (addressChanged && geolocationRepository.existsByAddressId(dto.getAddressId())) {
            throw new DuplicateResourceException(
                    "Geolocalização já cadastrada para o endereço: " + dto.getAddressId());
        }

        geolocation.setAddress(address);
        geolocation.setLatitude(dto.getLatitude());
        geolocation.setLongitude(dto.getLongitude());

        return toResponse(geolocationRepository.save(geolocation));
    }

    private Address resolveAddress(UUID addressId) {
        if (addressId == null) {
            throw new InvalidFieldException("ID do Endereço é obrigatório");
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado com ID: " + addressId));
        rbac.requireOwnCompany(address.getOwnerCompany().getId());
        return address;
    }

    @Transactional
    public void deleteById(UUID id) {
        Geolocation geolocation = geolocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geolocalização não encontrada com ID: " + id));
        rbac.requireOwnCompany(geolocation.getAddress().getOwnerCompany().getId());
        geolocationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public GeolocationResponseDTO findById(UUID id) {
        Geolocation geolocation = geolocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geolocalização não encontrada com ID: " + id));
        rbac.requireOwnCompany(geolocation.getAddress().getOwnerCompany().getId());
        return toResponse(geolocation);
    }

    @Transactional(readOnly = true)
    public List<GeolocationResponseDTO> findByAddress(UUID addressId) {
        resolveAddress(addressId);
        return geolocationRepository.findByAddressId(addressId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRequiredFields(GeolocationRequestDTO dto) {
        if (dto.getAddressId() == null) {
            throw new InvalidFieldException("ID do Endereço é obrigatório");
        }
        if (dto.getLatitude() == null) {
            throw new InvalidFieldException("Latitude é obrigatória");
        }
        if (dto.getLongitude() == null) {
            throw new InvalidFieldException("Longitude é obrigatória");
        }
    }

    private void validateCoordinateRanges(BigDecimal latitude, BigDecimal longitude) {
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new InvalidFieldException("Latitude inválida: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new InvalidFieldException("Longitude inválida: " + longitude);
        }
    }

    // Monta o DTO à mão: converter a entidade com o ObjectMapper segue as relações bidirecionais
    // e estoura a pilha com "Infinite recursion" (GET respondia 500).
    private GeolocationResponseDTO toResponse(Geolocation geolocation) {
        GeolocationResponseDTO response = new GeolocationResponseDTO();
        response.setId(geolocation.getId());
        response.setAddressId(geolocation.getAddress() == null ? null : geolocation.getAddress().getId());
        response.setLatitude(geolocation.getLatitude());
        response.setLongitude(geolocation.getLongitude());
        return response;
    }
}
