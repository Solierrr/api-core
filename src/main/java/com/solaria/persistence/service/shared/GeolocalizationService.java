package com.solaria.persistence.service.shared;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.shared.Geolocalization;
import com.solaria.persistence.dto.request.shared.GeolocalizationRequestDTO;
import com.solaria.persistence.dto.response.shared.GeolocalizationResponseDTO;
import com.solaria.persistence.exception.DuplicateResourceException;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.shared.AddressRepository;
import com.solaria.persistence.repository.shared.GeolocalizationRepository;

@Service
public class GeolocalizationService {

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");

    private final GeolocalizationRepository geolocalizationRepository;
    private final AddressRepository addressRepository;
    private final ObjectMapper objectMapper;
    private final RbacAuthorizationService rbac;

    public GeolocalizationService(GeolocalizationRepository geolocalizationRepository,
                                  AddressRepository addressRepository,
                                  ObjectMapper objectMapper, RbacAuthorizationService rbac) {
        this.rbac = rbac;
        this.geolocalizationRepository = geolocalizationRepository;
        this.addressRepository = addressRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GeolocalizationResponseDTO save(GeolocalizationRequestDTO dto) {
        validateRequiredFields(dto);
        validateCoordinateRanges(dto.getLatitude(), dto.getLongitude());

        Address address = resolveAddress(dto.getAddressId());
        if (geolocalizationRepository.existsByAddressId(dto.getAddressId())) {
            throw new DuplicateResourceException(
                    "Geolocalização já cadastrada para o endereço: " + dto.getAddressId());
        }

        Geolocalization geolocalization = new Geolocalization();
        geolocalization.setAddress(address);
        geolocalization.setLatitude(dto.getLatitude());
        geolocalization.setLongitude(dto.getLongitude());

        return toResponse(geolocalizationRepository.save(geolocalization));
    }

    @Transactional
    public GeolocalizationResponseDTO update(UUID id, GeolocalizationRequestDTO dto) {
        Geolocalization geolocalization = geolocalizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geolocalização com id:" + id + " não encontrada para atualização"));

        validateRequiredFields(dto);
        validateCoordinateRanges(dto.getLatitude(), dto.getLongitude());

        Address currentAddress = geolocalization.getAddress();
        rbac.requireOwnCompany(currentAddress.getOwnerCompany().getId());
        Address address = resolveAddress(dto.getAddressId());
        boolean addressChanged = !dto.getAddressId().equals(currentAddress.getId());

        if (addressChanged && geolocalizationRepository.existsByAddressId(dto.getAddressId())) {
            throw new DuplicateResourceException(
                    "Geolocalização já cadastrada para o endereço: " + dto.getAddressId());
        }

        geolocalization.setAddress(address);
        geolocalization.setLatitude(dto.getLatitude());
        geolocalization.setLongitude(dto.getLongitude());

        return toResponse(geolocalizationRepository.save(geolocalization));
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
        Geolocalization geolocalization = geolocalizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geolocalização não encontrada com ID: " + id));
        rbac.requireOwnCompany(geolocalization.getAddress().getOwnerCompany().getId());
        geolocalizationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public GeolocalizationResponseDTO findById(UUID id) {
        Geolocalization geolocalization = geolocalizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Geolocalização não encontrada com ID: " + id));
        rbac.requireOwnCompany(geolocalization.getAddress().getOwnerCompany().getId());
        return toResponse(geolocalization);
    }

    @Transactional(readOnly = true)
    public List<GeolocalizationResponseDTO> findByAddress(UUID addressId) {
        resolveAddress(addressId);
        return geolocalizationRepository.findByAddressId(addressId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRequiredFields(GeolocalizationRequestDTO dto) {
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

    private GeolocalizationResponseDTO toResponse(Geolocalization geolocalization) {
        GeolocalizationResponseDTO response = objectMapper.convertValue(geolocalization, GeolocalizationResponseDTO.class);
        response.setAddressId(geolocalization.getAddress() == null ? null : geolocalization.getAddress().getId());
        return response;
    }
}
