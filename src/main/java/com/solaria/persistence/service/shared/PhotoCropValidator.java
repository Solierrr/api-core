package com.solaria.persistence.service.shared;

import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.request.shared.PhotoCropRequestDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.service.shared.CloudinaryService.ImageInfo;

public final class PhotoCropValidator {

    private static final double BANNER_RATIO = 3.0;
    private static final double BANNER_TOLERANCE = 0.05;

    private PhotoCropValidator() {
    }

    /** Perfil tem que ser quadrado, banner 3:1 (com folga), e o retangulo precisa caber na imagem original. */
    public static void validate(PhotoType type, PhotoCropRequestDTO crop, ImageInfo image) {
        if (crop.x() + crop.width() > image.width() || crop.y() + crop.height() > image.height()) {
            throw new InvalidFieldException("O recorte excede as dimensões da imagem (" + image.width() + "x" + image.height() + ")");
        }
        if (type == PhotoType.PROFILE && Math.abs(crop.width() - crop.height()) > 1) {
            throw new InvalidFieldException("O recorte da foto de perfil deve ser quadrado");
        }
        if (type == PhotoType.BANNER) {
            double ratio = (double) crop.width() / crop.height();
            if (Math.abs(ratio - BANNER_RATIO) / BANNER_RATIO > BANNER_TOLERANCE) {
                throw new InvalidFieldException("O recorte do banner deve ter proporção 3:1");
            }
        }
    }
}
