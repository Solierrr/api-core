package com.solaria.persistence.dto.response.shared;

public record PhotoCropDTO(int x, int y, int width, int height) {

    /** Os quatro valores sao gravados juntos; qualquer nulo significa "sem recorte" (automatico). */
    public static PhotoCropDTO of(Integer x, Integer y, Integer width, Integer height) {
        if (x == null || y == null || width == null || height == null) {
            return null;
        }
        return new PhotoCropDTO(x, y, width, height);
    }
}
