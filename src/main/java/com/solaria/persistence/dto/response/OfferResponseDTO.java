package com.solaria.persistence.dto.response;

import com.solaria.persistence.domain.enums.TranslationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OfferResponseDTO {

    private UUID id;
    private UUID supplierId;
    private ModelResponseDTO model;
    private String slug;
    private BigDecimal unitPrice;
    private Integer availability;
    private OffsetDateTime expirationDate;
    private BigDecimal discountPercentage;
    private List<String> serviceRegions;
    private String sourceLocale;
    private TranslationStatus translationStatus;
    private List<OfferTranslationResponseDTO> translations;

}
