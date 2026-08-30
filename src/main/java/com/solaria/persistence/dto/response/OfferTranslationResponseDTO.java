package com.solaria.persistence.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferTranslationResponseDTO {

    private String locale;
    private String title;
    private String description;
    private String details;

}
