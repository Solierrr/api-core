package com.solaria.persistence.service.catalog;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.enums.catalog.InfrastructureType;
import com.solaria.persistence.domain.enums.catalog.ProductType;
import com.solaria.persistence.dto.response.catalog.InfrastructureOfferResponseDTO;
import com.solaria.persistence.dto.response.catalog.InternalProfessionResponseDTO;
import com.solaria.persistence.dto.response.professional.AccreditedTechnicianResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.professional.ProfessionRepository;
import com.solaria.persistence.repository.professional.TechnicianAffiliationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalCatalogService {
    static final int MAX_OFFERS = 20;
    static final int MAX_TECHNICIANS = 100;

    private final ProfessionRepository professionRepository;
    private final OfferRepository offerRepository;
    private final OfferCatalogService offerCatalogService;
    private final TechnicianAffiliationRepository technicianAffiliationRepository;

    @Transactional(readOnly = true)
    public List<InternalProfessionResponseDTO> professions() {
        return professionRepository.findAll(Sort.by("name")).stream()
                .map(profession -> new InternalProfessionResponseDTO(profession.getId(), profession.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InfrastructureOfferResponseDTO> infrastructureOffers(InfrastructureType infrastructureType,
            String brand, int limit) {
        requireLimit(limit, MAX_OFFERS);
        String brandFilter = normalize(brand);
        return offerRepository.findByExpirationDateIsNullOrExpirationDateAfter(OffsetDateTime.now()).stream()
                .filter(offer -> offer.getModel().getProductType() == ProductType.INFRASTRUCTURE)
                .filter(offerCatalogService::visible)
                .filter(offer -> infrastructureType == null
                        || offer.getModel().getInfrastructureType() == infrastructureType)
                .filter(offer -> brandFilter == null || (offer.getModel().getBrand() != null
                        && offer.getModel().getBrand().toLowerCase(Locale.ROOT).contains(brandFilter)))
                .sorted(Comparator.comparing(Offer::getUnitPrice).thenComparing(offer -> offer.getId().toString()))
                .limit(limit)
                .map(this::toInfrastructureOffer)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccreditedTechnicianResponseDTO> accreditedTechnicians(String profession, String city, int limit) {
        requireLimit(limit, MAX_TECHNICIANS);
        String professionFilter = normalize(profession);
        String cityFilter = normalize(city);
        return technicianAffiliationRepository.findAccredited(
                professionFilter == null, like(professionFilter),
                cityFilter == null, like(cityFilter),
                Limit.of(limit));
    }

    private InfrastructureOfferResponseDTO toInfrastructureOffer(Offer offer) {
        var model = offer.getModel();
        return new InfrastructureOfferResponseDTO(
                offer.getId(),
                offer.getSupplier().getCompany().getTradeName(),
                model.getBrand(),
                model.getModel(),
                model.getInfrastructureType() == null ? null : model.getInfrastructureType().name(),
                offer.getUnitPrice().toPlainString(),
                offer.getAvailability(),
                offer.getExpirationDate());
    }

    private static void requireLimit(int limit, int max) {
        if (limit < 1 || limit > max) {
            throw new InvalidFieldException("limit deve estar entre 1 e " + max);
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String like(String value) {
        return value == null ? "" : "%" + value + "%";
    }
}
