package com.solaria.persistence.dto.patch.proposal;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateNotesDTO {

    @Size(max = 500, message = "Notas excedem o tamanho máximo permitido")
    private String notes;

}
