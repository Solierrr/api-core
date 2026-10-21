package com.solaria.persistence.dto.request.shared;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Retangulo de recorte (ponto focal), em pixels da imagem original. */
public record PhotoCropRequestDTO(@NotNull @Min(0) Integer x, @NotNull @Min(0) Integer y,
                                  @NotNull @Min(1) Integer width, @NotNull @Min(1) Integer height) {
}
