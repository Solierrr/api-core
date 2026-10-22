package com.solaria.persistence.service;

import com.solaria.persistence.domain.entity.execution.ProjectLocation;
import com.solaria.persistence.service.execution.ProjectGeocodingService;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ProjectCoordinatesTest {
    @Test void rejectsMissingNonFiniteAndOutOfRangeCoordinates() {
        assertFalse(ProjectGeocodingService.valid(null, 1.0));
        assertFalse(ProjectGeocodingService.valid(Double.NaN, 1.0));
        assertFalse(ProjectGeocodingService.valid(91.0, 0.0));
        assertFalse(ProjectGeocodingService.valid(0.0, 181.0));
        assertTrue(ProjectGeocodingService.valid(-23.55, -46.63));
    }
    @Test void expiredGoogleCoordinatesAreNotExposed() {
        var location = new ProjectLocation();
        location.setLatitude(-23.55); location.setLongitude(-46.63);
        location.setGeocodedAt(OffsetDateTime.now().minusDays(30));
        assertNull(location.getLatitude()); assertNull(location.getLongitude());
        assertFalse(ProjectGeocodingService.fresh(location));
    }
    @Test void ownCoordinatesDoNotGetAnInventedExpiration() {
        var location = new ProjectLocation();
        location.setLatitude(-23.55); location.setLongitude(-46.63);
        assertTrue(ProjectGeocodingService.fresh(location));
    }
}
