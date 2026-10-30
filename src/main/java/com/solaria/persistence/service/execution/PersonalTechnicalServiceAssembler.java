package com.solaria.persistence.service.execution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.solaria.persistence.domain.entity.execution.ServiceExecutor;
import com.solaria.persistence.domain.entity.execution.TechnicalService;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.identity.UserPhoto;
import com.solaria.persistence.domain.entity.shared.Address;
import com.solaria.persistence.domain.entity.shared.Geolocation;
import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceAddressResponseDTO;
import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceExecutorResponseDTO;
import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceGeoResponseDTO;
import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceResponseDTO;
import com.solaria.persistence.repository.execution.ServiceExecutorRepository;
import com.solaria.persistence.repository.identity.UserPhotoRepository;
import com.solaria.persistence.repository.shared.GeolocationRepository;
import com.solaria.persistence.security.mobile.ActorServiceAccess;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PersonalTechnicalServiceAssembler {

    private final ServiceExecutorRepository serviceExecutorRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final GeolocationRepository geolocationRepository;

    public List<ServiceExecutor> loadCrew(Collection<TechnicalService> services) {
        List<UUID> serviceIds = services.stream().map(TechnicalService::getId).distinct().toList();
        return serviceIds.isEmpty() ? List.of() : serviceExecutorRepository.findWithCrewByServiceIds(serviceIds);
    }

    public List<PersonalTechnicalServiceResponseDTO> assembleAll(
            List<TechnicalService> services,
            Map<UUID, ActorServiceAccess> accessByServiceId,
            List<ServiceExecutor> crew) {
        if (services.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<ServiceExecutor>> crewByService = groupCrewByService(crew);
        Map<UUID, String> profilePhotoUrls = loadProfilePhotoUrls(crew);
        Map<UUID, PersonalTechnicalServiceGeoResponseDTO> geolocations = loadGeolocations(services);

        return services.stream()
                .map(service -> assembleOne(
                        service,
                        requiredAccess(accessByServiceId, service.getId()),
                        crewByService.getOrDefault(service.getId(), List.of()),
                        profilePhotoUrls,
                        geolocations))
                .toList();
    }

    public PersonalTechnicalServiceResponseDTO assembleOne(
            TechnicalService service, ActorServiceAccess access, List<ServiceExecutor> crew) {
        return assembleAll(List.of(service), Map.of(service.getId(), access), crew).get(0);
    }

    private PersonalTechnicalServiceResponseDTO assembleOne(
            TechnicalService service,
            ActorServiceAccess access,
            List<ServiceExecutor> crew,
            Map<UUID, String> profilePhotoUrls,
            Map<UUID, PersonalTechnicalServiceGeoResponseDTO> geolocations) {
        PersonalTechnicalServiceResponseDTO response = new PersonalTechnicalServiceResponseDTO();
        response.setId(service.getId());
        response.setTechnicalProjectId(service.getTechnicalProject().getId());
        response.setPurpose(service.getPurpose());
        response.setStatus(service.getStatus());
        response.setScheduledDate(service.getScheduledDate());
        response.setCreatedAt(service.getCreatedAt());
        response.setAcceptedBy(service.getAcceptedBy());
        response.setAcceptedAt(service.getAcceptedAt());
        response.setEndDate(service.getEndDate());

        var technicalProject = service.getTechnicalProject();
        if (technicalProject.getRequester() != null && technicalProject.getRequester().getCompany() != null) {
            response.setRequesterName(technicalProject.getRequester().getCompany().getTradeName());
        }

        LocalUnit localUnit = technicalProject.getLocalUnit();
        if (localUnit != null) {
            Address address = localUnit.getAddress();
            if (address != null) {
                response.setAddress(new PersonalTechnicalServiceAddressResponseDTO(
                        address.getStreet(),
                        address.getNumber(),
                        localUnit.getComplement(),
                        address.getNeighborhood(),
                        address.getCity(),
                        address.getState(),
                        address.getZipCode()));
                response.setGeo(geolocations.get(address.getId()));
            }
        }

        response.setExecutors(crew.stream()
                .map(executor -> toExecutorResponse(executor, profilePhotoUrls))
                .toList());
        response.setMyRole(access.myRole());
        response.setAllowedActions(access.allowedActions(service.getStatus(), !crew.isEmpty()));
        return response;
    }

    private static ActorServiceAccess requiredAccess(Map<UUID, ActorServiceAccess> accessByServiceId, UUID serviceId) {
        ActorServiceAccess access = accessByServiceId.get(serviceId);
        if (access == null) {
            throw new IllegalStateException("Fatos de acesso ausentes para o serviço " + serviceId);
        }
        return access;
    }

    private static Map<UUID, List<ServiceExecutor>> groupCrewByService(
            Collection<ServiceExecutor> crew) {
        Map<UUID, List<ServiceExecutor>> crewByService = new LinkedHashMap<>();
        for (ServiceExecutor executor : crew) {
            UUID serviceId = executor.getService().getId();
            crewByService.computeIfAbsent(serviceId, ignored -> new ArrayList<>()).add(executor);
        }
        return crewByService;
    }

    private Map<UUID, String> loadProfilePhotoUrls(Collection<ServiceExecutor> crew) {
        Set<UUID> userIds = crew.stream()
                .map(executor -> executor.getTechnicianAffiliation().getTechnician().getPerson().getUser())
                .filter(user -> user != null)
                .map(User::getId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, String> photoUrls = new HashMap<>();
        for (UserPhoto photo : userPhotoRepository.findPhotosByUserIdsAndType(userIds, PhotoType.PROFILE)) {
            photoUrls.putIfAbsent(photo.getUser().getId(), photo.getUrl());
        }
        return photoUrls;
    }

    private Map<UUID, PersonalTechnicalServiceGeoResponseDTO> loadGeolocations(
            Collection<TechnicalService> services) {
        Set<UUID> addressIds = services.stream()
                .map(TechnicalService::getTechnicalProject)
                .filter(project -> project != null && project.getLocalUnit() != null)
                .map(project -> project.getLocalUnit().getAddress())
                .filter(address -> address != null)
                .map(Address::getId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (addressIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<Geolocation>> byAddress = new HashMap<>();
        for (Geolocation item : geolocationRepository.findWithAddressByAddressIds(addressIds)) {
                byAddress.computeIfAbsent(item.getAddress().getId(), ignored -> new ArrayList<>()).add(item);
        }

        Map<UUID, PersonalTechnicalServiceGeoResponseDTO> result = new HashMap<>();
        byAddress.forEach((addressId, items) -> resolveGeo(items)
                .ifPresent(geo -> result.put(addressId, geo)));
        return result;
    }

    private static Optional<PersonalTechnicalServiceGeoResponseDTO> resolveGeo(
            List<Geolocation> geolocations) {
        Map<CoordinateKey, Geolocation> uniqueCoordinates = new LinkedHashMap<>();
        for (Geolocation item : geolocations) {
            if (item.getLatitude() == null || item.getLongitude() == null) {
                return Optional.empty();
            }
            CoordinateKey key = new CoordinateKey(
                    item.getLatitude().stripTrailingZeros(),
                    item.getLongitude().stripTrailingZeros());
            uniqueCoordinates.putIfAbsent(key, item);
        }
        if (uniqueCoordinates.size() != 1) {
            return Optional.empty();
        }

        Geolocation coordinate = uniqueCoordinates.values().iterator().next();
        return Optional.of(new PersonalTechnicalServiceGeoResponseDTO(
                coordinate.getLatitude(), coordinate.getLongitude()));
    }

    private static PersonalTechnicalServiceExecutorResponseDTO toExecutorResponse(
            ServiceExecutor executor, Map<UUID, String> profilePhotoUrls) {
        Person person = executor.getTechnicianAffiliation().getTechnician().getPerson();
        User user = person.getUser();
        UUID authId = user == null ? null : user.getAuthId();
        String avatarUrl = user == null ? null : profilePhotoUrls.getOrDefault(user.getId(), user.getAvatar());
        return new PersonalTechnicalServiceExecutorResponseDTO(
                authId,
                person.getName(),
                executor.getFunction(),
                avatarUrl);
    }

    private record CoordinateKey(BigDecimal latitude, BigDecimal longitude) {
    }
}
