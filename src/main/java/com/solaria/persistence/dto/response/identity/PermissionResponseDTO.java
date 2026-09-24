package com.solaria.persistence.dto.response.identity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PermissionResponseDTO {

    private UUID id;
    private String permissionName;
    private String name;
    private String description;

}
