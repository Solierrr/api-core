package com.solaria.persistence.service.catalog;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.OfferTranslation;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.domain.enums.catalog.ProductType;
import com.solaria.persistence.domain.enums.catalog.SupplierStatus;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.company.CompanyType;
import com.solaria.persistence.dto.response.catalog.CatalogOfferResponseDTO;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.ModelPhotoRepository;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.catalog.OfferTranslationRepository;

@Service
public class OfferCatalogService {
    private final OfferRepository offerRepository;
    private final OfferTranslationRepository offerTranslationRepository;
    private final ModelPhotoRepository modelPhotoRepository;

    public OfferCatalogService(OfferRepository offerRepository,
            OfferTranslationRepository offerTranslationRepository,
            ModelPhotoRepository modelPhotoRepository) {
        this.offerRepository = offerRepository;
        this.offerTranslationRepository = offerTranslationRepository;
        this.modelPhotoRepository = modelPhotoRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogOfferResponseDTO> findAll(String locale) {
        return findAll(locale, null);
    }

    @Transactional(readOnly = true)
    public List<CatalogOfferResponseDTO> findAll(String locale, ProductType productType) {
        return offerRepository.findByExpirationDateIsNullOrExpirationDateAfter(OffsetDateTime.now())
                .stream()
                .filter(this::visible)
                .filter(offer -> productType == null || offer.getModel().getProductType() == productType)
                .map(offer -> toResponse(offer, locale))
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogOfferResponseDTO findBySlug(String companySlug, String offerSlug, String locale) {
        Offer offer = offerRepository.findBySlugAndSupplier_Company_Slug(offerSlug, companySlug)
                .filter(this::visible)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta não encontrada"));
        return toResponse(offer, locale);
    }

    boolean visible(Offer offer) {
        return offer.getCommercialStatus() == com.solaria.persistence.domain.enums.catalog.OfferStatus.ACTIVE
                && offer.getSupplier().getStatus() == SupplierStatus.ACTIVE
                && offer.getSupplier().getCompany().getStatus() == CompanyStatus.APPROVED
                && offer.getSupplier().getCompany().getType() == CompanyType.SUPPLIER
                && offer.getModel().getStatus() == ModelStatus.APPROVED
                && offer.getAvailability() != null && offer.getAvailability() > 0
                && (offer.getExpirationDate() == null || offer.getExpirationDate().isAfter(OffsetDateTime.now()));
    }

    private CatalogOfferResponseDTO toResponse(Offer offer, String locale) {
        List<OfferTranslation> translations = offerTranslationRepository.findByOfferId(offer.getId());
        OfferTranslation translation = translations.stream()
                .filter(item -> Objects.equals(item.getLocale(), locale))
                .findFirst()
                .or(() -> translations.stream()
                        .filter(item -> Objects.equals(item.getLocale(), offer.getSourceLocale()))
                        .findFirst())
                .or(() -> translations.stream().filter(item -> Objects.equals(item.getLocale(), OfferTranslationService.PENDING_LOCALE)).findFirst())
                .or(() -> translations.stream().findFirst())
                .orElse(null);
        String title = translation == null
                ? offer.getModel().getBrand() + " " + offer.getModel().getModel()
                : translation.getTitle();
        var company = offer.getSupplier().getCompany();
        var model = offer.getModel();
        List<String> photos = modelPhotoRepository.findByModelIdOrderByCreatedAtDesc(model.getId())
                .stream().sorted(java.util.Comparator.comparing(
                        com.solaria.persistence.domain.entity.catalog.ModelPhoto::isPrimary).reversed())
                .map(photo -> photo.getUrl()).toList();
        return new CatalogOfferResponseDTO(
                offer.getId(), offer.getSlug(), company.getId(), company.getSlug(), company.getTradeName(),
                title,
                translation == null ? "" : translation.getDescription(),
                translation == null ? "" : translation.getDetails(),
                model.getId(), model.getBrand(), model.getModel(), model.getProductType().name(),
                model.getInfrastructureType() == null ? null : model.getInfrastructureType().name(), model.getMaterial(),
                model.getType() == null ? null : model.getType().name(), model.getPowerWp(),
                model.getEfficiency(), model.getWidth(), model.getLength(), model.getWeight(),
                offer.getUnitPrice(), offer.getDiscountPercentage(), offer.getAvailability(),
                offer.getServiceRegions(), photos);
    }
}
