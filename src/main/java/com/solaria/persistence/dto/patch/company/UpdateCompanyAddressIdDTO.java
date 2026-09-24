package com.solaria.persistence.dto.patch.company;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UpdateCompanyAddressIdDTO {

    @NotNull(message = "ID do Endereço é obrigatório")
    private UUID addressId;

}
