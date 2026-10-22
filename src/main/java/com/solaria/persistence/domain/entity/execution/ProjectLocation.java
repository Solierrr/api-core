package com.solaria.persistence.domain.entity.execution;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Embeddable
@Getter
@Setter
public class ProjectLocation {
    @DecimalMin("-90") @DecimalMax("90")
    @Column(name = "location_latitude")
    private Double latitude;

    @DecimalMin("-180") @DecimalMax("180")
    @Column(name = "location_longitude")
    private Double longitude;

    @Size(max = 300)
    @Column(name = "location_place_id")
    private String placeId;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    @Column(name = "location_geocoded_at")
    private java.time.OffsetDateTime geocodedAt;

    @AssertTrue(message = "Latitude e longitude devem ser informadas juntas")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isCoordinatePairValid() { return (latitude == null) == (longitude == null); }

    @Size(max = 2)
    @Pattern(regexp = "(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)|^$")
    @Column(name = "location_state")
    private String state;

    @Size(max = 120)
    @Column(name = "location_city")
    private String city;

    @Size(max = 120)
    @Column(name = "location_neighborhood")
    private String neighborhood;

    @Pattern(regexp = "[0-9]{8}|^$")
    @Column(name = "location_zip_code")
    private String zipCode;

    @Size(max = 180)
    @Column(name = "location_street")
    private String street;

    @Size(max = 30)
    @Column(name = "location_number")
    private String number;

    @Size(max = 180)
    @Column(name = "location_complement")
    private String complement;

    public Double getLatitude() { return coordinatesExpired() ? null : latitude; }
    public Double getLongitude() { return coordinatesExpired() ? null : longitude; }
    private boolean coordinatesExpired() {
        return geocodedAt != null && !geocodedAt.isAfter(java.time.OffsetDateTime.now().minusDays(29));
    }

    public boolean isUsable() {
        return city != null
                && !city.isBlank()
                && street != null
                && !street.isBlank()
                && state != null
                && state.matches("AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO");
    }
}
