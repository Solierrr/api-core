package com.solaria.persistence.dto.request.identity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedeemAccessCodeRequestDTO {

    @NotBlank(message = "Código é obrigatório")
    private String code;

}
