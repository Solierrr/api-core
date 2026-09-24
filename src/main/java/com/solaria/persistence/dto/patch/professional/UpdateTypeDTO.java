package com.solaria.persistence.dto.patch.professional;

import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateTypeDTO {

    @NotNull(message = "Tipo de afiliação é obrigatório")
    private TechnicalAffiliationType affiliationType;

}
