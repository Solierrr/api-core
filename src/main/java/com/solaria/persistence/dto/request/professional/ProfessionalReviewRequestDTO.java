package com.solaria.persistence.dto.request.professional;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProfessionalReviewRequestDTO {

    @NotNull(message = "ID do profissional é obrigatório")
    private UUID professionalId;

    @NotNull(message = "ID do avaliador é obrigatório")
    private UUID reviewerId;

    @NotNull(message = "ID do serviço concluído é obrigatório")
    private UUID serviceId;

    @NotNull(message = "Nota é obrigatória")
    @DecimalMin(value = "0", message = "Nota mínima é 0")
    @DecimalMax(value = "5", message = "Nota máxima é 5")
    private BigDecimal rating;

    @Size(max = 2000, message = "Comentário não pode exceder 2000 caracteres")
    private String comment;

}
