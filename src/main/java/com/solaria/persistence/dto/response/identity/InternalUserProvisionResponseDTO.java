package com.solaria.persistence.dto.response.identity;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserProvisionResponseDTO {

    private UUID id;

    private UUID authId;

    private boolean created;
}
