package com.solaria.persistence.service.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.company.CompanyPhoto;
import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.execution.ProjectRole;
import com.solaria.persistence.domain.enums.execution.ServiceStatus;
import com.solaria.persistence.domain.enums.shared.PhotoType;
import com.solaria.persistence.repository.company.CompanyPhotoRepository;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectOwnerLogoTest {
    @Mock ProjectAccess access;
    @Mock CompanyRepository companies;
    @Mock CompanyPhotoRepository companyPhotos;
    @Mock UserRepository users;
    @InjectMocks ProjectService service;

    TechnicalProject project;

    @BeforeEach
    void setup() {
        project = new TechnicalProject();
        project.setId(UUID.randomUUID());
        project.setName("Obra");
        project.setStatus(ServiceStatus.OPEN);
    }

    @Test
    void companyProjectCarriesTheCompanyProfilePhoto() {
        UUID companyId = UUID.randomUUID();
        project.setOwnerCompanyId(companyId);
        when(access.read(project)).thenReturn(ProjectRole.MANAGER);
        Company company = new Company();
        company.setTradeName("Solar Forte");
        when(companies.findById(companyId)).thenReturn(Optional.of(company));
        CompanyPhoto photo = new CompanyPhoto();
        photo.setUrl("https://res.cloudinary.com/x/logo.webp");
        when(companyPhotos.findByCompanyIdAndType(companyId, PhotoType.PROFILE)).thenReturn(Optional.of(photo));

        var response = service.response(project);

        assertEquals("Solar Forte", response.ownerName());
        assertEquals("https://res.cloudinary.com/x/logo.webp", response.ownerCompanyLogoUrl());
    }

    @Test
    void companyWithoutPhotoHasNoLogo() {
        UUID companyId = UUID.randomUUID();
        project.setOwnerCompanyId(companyId);
        when(access.read(project)).thenReturn(ProjectRole.MEMBER);
        when(companies.findById(companyId)).thenReturn(Optional.empty());
        when(companyPhotos.findByCompanyIdAndType(companyId, PhotoType.PROFILE)).thenReturn(Optional.empty());

        assertNull(service.response(project).ownerCompanyLogoUrl());
    }

    @Test
    void personalProjectDoesNotLookUpCompanyPhotos() {
        UUID userId = UUID.randomUUID();
        project.setOwnerUserId(userId);
        when(access.read(project)).thenReturn(ProjectRole.OWNER);
        User user = new User();
        user.setUsername("amanda");
        when(users.findById(userId)).thenReturn(Optional.of(user));

        var response = service.response(project);

        assertNull(response.ownerCompanyLogoUrl());
        verifyNoInteractions(companyPhotos);
    }
}
