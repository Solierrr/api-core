package com.solaria.persistence.dto.response.unit;

import com.solaria.persistence.domain.enums.unit.LocationType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import com.solaria.persistence.dto.response.shared.AddressResponseDTO;

@Getter
@Setter
public class LocalUnitResponseDTO {

    private UUID id;
    private UUID requesterId;
    private AddressResponseDTO address;
    private String complement;
    private LocationType locationType;

}
