package com.solaria.persistence.service;

import com.solaria.persistence.config.GoogleTranslateProperties;
import com.solaria.persistence.domain.entity.execution.ProjectLocation;
import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.service.execution.ProjectAccess;
import com.solaria.persistence.service.execution.ProjectGeocodingService;
import com.solaria.persistence.service.execution.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ProjectGeocodingServiceTest {
    private final ProjectService projects = mock(ProjectService.class);
    private final ProjectAccess access = mock(ProjectAccess.class);
    private final UUID id = UUID.randomUUID();
    private TechnicalProject project;
    private ProjectGeocodingService geocoding;
    private MockRestServiceServer server;

    @BeforeEach void setup() {
        var builder = RestClient.builder().baseUrl("http://registry.test");
        server = MockRestServiceServer.bindTo(builder).build();
        var properties = new GoogleTranslateProperties();
        properties.setConsumerToken("local-test-token");
        geocoding = new ProjectGeocodingService(projects, access, builder.build(), properties);
        project = new TechnicalProject();
        var location = new ProjectLocation();
        location.setStreet("Praça da Sé"); location.setCity("São Paulo"); location.setState("SP");
        project.setLocation(location);
        when(projects.entity(id, true)).thenReturn(project);
    }

    @Test void forbiddenProjectNeverCallsRegistry() {
        doThrow(new UnauthorizedAccessException("Sem acesso")).when(access).read(project);
        assertThrows(UnauthorizedAccessException.class, () -> geocoding.resolve(id));
        server.verify();
    }

    @Test void partialAddressNeverBecomesAConfirmedMarker() {
        server.expect(requestTo("http://registry.test/v1/address/geocode"))
                .andExpect(header("Authorization", "Bearer local-test-token"))
                .andRespond(withSuccess("{\"results\":[{\"latitude\":-23.55,\"longitude\":-46.63,\"partial_match\":true}]}", MediaType.APPLICATION_JSON));
        assertThrows(BusinessRuleException.class, () -> geocoding.resolve(id));
        assertNull(project.getLocation().getLatitude());
        server.verify();
    }

    @Test void confirmedCoordinatesAreReusedWithoutAnotherGoogleRequest() {
        server.expect(requestTo("http://registry.test/v1/address/geocode"))
                .andRespond(withSuccess("{\"results\":[{\"latitude\":-23.55,\"longitude\":-46.63,\"place_id\":\"test-place\",\"partial_match\":false}]}", MediaType.APPLICATION_JSON));
        geocoding.resolve(id);
        var timestamp = project.getLocation().getGeocodedAt();
        geocoding.resolve(id);
        assertEquals(-23.55, project.getLocation().getLatitude());
        assertEquals(timestamp, project.getLocation().getGeocodedAt());
        server.verify();
    }
}
