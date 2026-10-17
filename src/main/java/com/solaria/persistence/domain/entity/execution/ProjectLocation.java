package com.solaria.persistence.domain.entity.execution;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Embeddable
@Getter
@Setter
public class ProjectLocation {
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

    public boolean isUsable() {
        return city != null
                && !city.isBlank()
                && street != null
                && !street.isBlank()
                && state != null
                && state.matches("AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO");
    }
}
