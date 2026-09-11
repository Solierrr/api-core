package com.solaria.persistence.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserResponseDTO {

    private UUID id;
    private UUID authId;
    private String username;
    private String avatar;
    private String banner;
    private Boolean active;

}
