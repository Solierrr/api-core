package com.solaria.persistence.service.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.dto.request.shared.PhotoCropRequestDTO;
import com.solaria.persistence.dto.response.shared.PhotoCropDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.service.shared.CloudinaryService.ImageInfo;

class PhotoVariantsTest {

    private static final String URL = "https://res.cloudinary.com/demo/image/upload/v17/solaria/usuarios/u1/profile.webp";

    @Test
    void profileWithoutCropUsesFaceDetection() {
        Map<String, String> variants = PhotoVariants.of(URL, PhotoType.PROFILE, null);
        assertEquals(URL, variants.get("original"));
        assertEquals("https://res.cloudinary.com/demo/image/upload/c_thumb,g_face,w_64,h_64,f_auto,q_auto/v17/solaria/usuarios/u1/profile.webp", variants.get("avatarSm"));
        assertEquals("https://res.cloudinary.com/demo/image/upload/c_thumb,g_face,w_256,h_256,f_auto,q_auto/v17/solaria/usuarios/u1/profile.webp", variants.get("avatar"));
        assertFalse(variants.containsKey("banner"));
    }

    @Test
    void profileWithCropCropsFirstThenScales() {
        Map<String, String> variants = PhotoVariants.of(URL, PhotoType.PROFILE, new PhotoCropDTO(120, 40, 800, 800));
        assertEquals("https://res.cloudinary.com/demo/image/upload/c_crop,x_120,y_40,w_800,h_800/c_scale,w_256,f_auto,q_auto/v17/solaria/usuarios/u1/profile.webp", variants.get("avatar"));
    }

    @Test
    void bannerVariantsFollowTheStoredCropOrAutoFocus() {
        Map<String, String> automatic = PhotoVariants.of(URL, PhotoType.BANNER, null);
        assertTrue(automatic.get("banner").contains("c_fill,g_auto,ar_3:1,w_1600,f_auto,q_auto/"));
        assertTrue(automatic.get("card").contains("ar_16:9,w_640"));
        Map<String, String> cropped = PhotoVariants.of(URL, PhotoType.BANNER, new PhotoCropDTO(0, 100, 1500, 500));
        assertTrue(cropped.get("banner").contains("c_crop,x_0,y_100,w_1500,h_500/c_scale,w_1600,f_auto,q_auto/"));
        assertTrue(cropped.get("card").contains("c_crop,x_0,y_100,w_1500,h_500/c_fill,g_auto,ar_16:9,w_640"));
    }

    @Test
    void urlsOutsideTheCloudinaryUploadPathKeepOnlyTheOriginal() {
        assertEquals(Map.of("original", "https://example.com/a.png"), PhotoVariants.of("https://example.com/a.png", PhotoType.PROFILE, null));
        assertTrue(PhotoVariants.of(null, PhotoType.PROFILE, null).isEmpty());
    }

    @Test
    void cropDtoIsNullUnlessAllFourValuesExist() {
        assertNull(PhotoCropDTO.of(1, 2, 3, null));
        assertEquals(new PhotoCropDTO(1, 2, 3, 4), PhotoCropDTO.of(1, 2, 3, 4));
    }

    @Test
    void validatorRequiresSquareProfileThreeToOneBannerAndInsideImage() {
        ImageInfo image = new ImageInfo(2000, 1000);
        PhotoCropValidator.validate(PhotoType.PROFILE, new PhotoCropRequestDTO(100, 100, 500, 500), image);
        PhotoCropValidator.validate(PhotoType.BANNER, new PhotoCropRequestDTO(0, 0, 1800, 600), image);
        assertThrows(InvalidFieldException.class, () -> PhotoCropValidator.validate(PhotoType.PROFILE, new PhotoCropRequestDTO(0, 0, 500, 400), image));
        assertThrows(InvalidFieldException.class, () -> PhotoCropValidator.validate(PhotoType.BANNER, new PhotoCropRequestDTO(0, 0, 1000, 800), image));
        assertThrows(InvalidFieldException.class, () -> PhotoCropValidator.validate(PhotoType.PROFILE, new PhotoCropRequestDTO(1600, 0, 500, 500), image));
    }
}
