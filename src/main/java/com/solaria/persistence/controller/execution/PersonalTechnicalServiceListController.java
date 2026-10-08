package com.solaria.persistence.controller.execution;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solaria.persistence.dto.response.execution.PersonalTechnicalServiceResponseDTO;
import com.solaria.persistence.openapi.execution.PersonalTechnicalServiceListOpenApi;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.service.execution.PersonalTechnicalServiceListService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/technical-services")
@RequiredArgsConstructor
public class PersonalTechnicalServiceListController implements PersonalTechnicalServiceListOpenApi {

    private final CurrentUserService currentUserService;
    private final PersonalTechnicalServiceListService personalTechnicalServiceListService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<PersonalTechnicalServiceResponseDTO>> findMine() {
        return ResponseEntity.ok(personalTechnicalServiceListService
                .findForUser(currentUserService.getProvisionedCurrentUserId()));
    }
}
