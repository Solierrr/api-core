package com.solaria.persistence.dto.response.catalog;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InventoryResponseDTO {

    private UUID id;
    private UUID supplierId;
    private ModelResponseDTO model;
    private Integer quantity;

}
