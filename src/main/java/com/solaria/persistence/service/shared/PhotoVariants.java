package com.solaria.persistence.service.shared;

import java.util.LinkedHashMap;
import java.util.Map;

import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.response.shared.PhotoCropDTO;

/**
 * Monta as variantes de uma foto a partir da URL base do Cloudinary e do recorte salvo.
 *
 * <p>Com recorte, a transformacao comeca por um crop explicito ({@code c_crop,x_,y_,w_,h_}); sem recorte, usa
 * deteccao de rosto (perfil) ou foco automatico (banner). O front so escolhe o nome da variante.</p>
 */
public final class PhotoVariants {

    private static final String UPLOAD_SEGMENT = "/upload/";
    private static final String DELIVERY = "f_auto,q_auto";

    private PhotoVariants() {
    }

    public static Map<String, String> of(String url, PhotoType type, PhotoCropDTO crop) {
        Map<String, String> variants = new LinkedHashMap<>();
        if (url == null) {
            return variants;
        }
        variants.put("original", url);
        int split = url.indexOf(UPLOAD_SEGMENT);
        if (split < 0) {
            return variants;
        }
        String base = url.substring(0, split + UPLOAD_SEGMENT.length());
        String asset = url.substring(split + UPLOAD_SEGMENT.length());
        String cropStep = crop == null ? null : "c_crop,x_%d,y_%d,w_%d,h_%d".formatted(crop.x(), crop.y(), crop.width(), crop.height());

        if (type == PhotoType.PROFILE) {
            variants.put("avatarSm", build(base, asset, cropStep, "c_thumb,g_face,w_64,h_64", "c_scale,w_64"));
            variants.put("avatar", build(base, asset, cropStep, "c_thumb,g_face,w_256,h_256", "c_scale,w_256"));
        } else {
            variants.put("card", build(base, asset, cropStep, "c_fill,g_auto,ar_16:9,w_640", "c_fill,g_auto,ar_16:9,w_640"));
            variants.put("banner", build(base, asset, cropStep, "c_fill,g_auto,ar_3:1,w_1600", "c_scale,w_1600"));
        }
        return variants;
    }

    private static String build(String base, String asset, String cropStep, String automatic, String afterCrop) {
        if (cropStep == null) {
            return base + automatic + "," + DELIVERY + "/" + asset;
        }
        return base + cropStep + "/" + afterCrop + "," + DELIVERY + "/" + asset;
    }
}
