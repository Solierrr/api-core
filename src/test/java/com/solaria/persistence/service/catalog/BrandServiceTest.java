package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solaria.persistence.domain.entity.catalog.*;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.catalog.*;
import com.solaria.persistence.dto.request.catalog.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.catalog.*;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

import org.junit.jupiter.api.*;

import java.util.*;

class BrandServiceTest {
    BrandRepository brands;
    BrandRequestRepository requests;
    ModelRepository models;
    RbacAuthorizationService rbac;
    CurrentUserService users;
    BrandService service;
    UUID company = UUID.randomUUID();
    Brand brand;
    BrandRequest request;

    @BeforeEach
    void setup() {
        brands = mock(BrandRepository.class);
        requests = mock(BrandRequestRepository.class);
        models = mock(ModelRepository.class);
        rbac = mock(RbacAuthorizationService.class);
        users = mock(CurrentUserService.class);
        service = new BrandService(brands, requests, models, rbac, users);
        when(rbac.getCurrentCompanyId()).thenReturn(company);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(users.getCurrentUser()).thenReturn(user);
        brand = new Brand();
        brand.setId(UUID.randomUUID());
        brand.setOwnerCompanyId(company);
        brand.setName("Solar Brand");
        brand.setNormalizedName("solar brand");
        request = new BrandRequest();
        request.setId(UUID.randomUUID());
        request.setBrand(brand);
        request.setRequesterCompanyId(company);
        request.setKind(BrandRequestKind.PUBLICATION);
        request.setProposedName("Solar Brand");
        when(brands.lockById(brand.getId())).thenReturn(Optional.of(brand));
        when(requests.findById(request.getId())).thenReturn(Optional.of(request));
        when(requests.lockById(request.getId())).thenReturn(Optional.of(request));
        when(brands.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(requests.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void privateSelectionIsScopedToCompanyAndArchivedCannotCreateLinks() {
        assertTrue(service.canSelect(brand, company));
        assertFalse(service.canSelect(brand, UUID.randomUUID()));
        brand.setArchived(true);
        assertFalse(service.canSelect(brand, company));
        assertThrows(BusinessRuleException.class, () -> service.selectable(brand.getId(), company));
    }

    @Test
    void legacyBrandsCannotBeSelectedWithoutApproval() {
        brand.setLegacy(true);
        brand.setOwnerCompanyId(null);
        assertFalse(service.canSelect(brand, company));
    }

    @Test
    void publicationKeepsPrivateUntilDecision() {
        BrandRequestDTO d = new BrandRequestDTO();
        d.setName("Solar Brand");
        service.submit(brand.getId(), d);
        assertEquals(BrandVisibility.PRIVATE, brand.getVisibility());
        assertTrue(service.canSelect(brand, company));
    }

    @Test
    void pendingRequestBlocksEditing() {
        when(requests.existsByBrandIdAndStatus(brand.getId(), BrandRequestStatus.PENDING))
                .thenReturn(true);
        BrandRequestDTO d = new BrandRequestDTO();
        d.setName("Other");
        assertThrows(BusinessRuleException.class, () -> service.update(brand.getId(), d));
        assertEquals("Solar Brand", brand.getName());
    }

    @Test
    void rejectionRequiresReasonAndPreservesPrivateBrand() {
        assertThrows(
                InvalidFieldException.class,
                () -> service.decide(request.getId(), new BrandDecisionDTO(0L, " "), false));
        service.decide(request.getId(), new BrandDecisionDTO(0L, "Referência insuficiente"), false);
        assertEquals(BrandVisibility.PRIVATE, brand.getVisibility());
        assertEquals(BrandRequestStatus.REJECTED, request.getStatus());
        assertEquals("Referência insuficiente", request.getDecisionReason());
    }

    @Test
    void staleOrRepeatedDecisionCannotApprove() {
        assertThrows(
                BusinessRuleException.class,
                () -> service.decide(request.getId(), new BrandDecisionDTO(1L, null), true));
        request.setStatus(BrandRequestStatus.CANCELLED);
        assertThrows(
                BusinessRuleException.class,
                () -> service.decide(request.getId(), new BrandDecisionDTO(0L, null), true));
        assertEquals(BrandVisibility.PRIVATE, brand.getVisibility());
    }

    @Test
    void approvedDuplicateReusesPublicCanonicalWithoutReplacingModels() {
        Brand canonical = new Brand();
        canonical.setId(UUID.randomUUID());
        canonical.setName("Solar Brand");
        canonical.setVisibility(BrandVisibility.PUBLIC);
        when(brands.findByNormalizedNameAndVisibility("solar brand", BrandVisibility.PUBLIC))
                .thenReturn(Optional.of(canonical));
        service.decide(request.getId(), new BrandDecisionDTO(0L, null), true);
        verify(models).reassignBrand(brand.getId(), canonical.getId());
        assertEquals(canonical.getId(), request.getResultBrandId());
        assertTrue(brand.isArchived());
    }

    @Test
    void publicCorrectionDoesNotReplaceNameBeforeApprovalAndCannotArchive() {
        brand.setVisibility(BrandVisibility.PUBLIC);
        request.setKind(BrandRequestKind.CORRECTION);
        request.setProposedName("Corrected");
        assertEquals("Solar Brand", brand.getName());
        assertThrows(BusinessRuleException.class, () -> service.archive(brand.getId()));
        service.decide(request.getId(), new BrandDecisionDTO(0L, null), true);
        assertEquals("Corrected", brand.getName());
        assertEquals(BrandVisibility.PUBLIC, brand.getVisibility());
    }

    @Test
    void foreignCompanyCannotCancelRequest() {
        doThrow(new UnauthorizedAccessException("Outro vínculo"))
                .when(rbac)
                .requireOwnCompany(company);
        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.cancel(request.getId(), new BrandDecisionDTO(0L, null)));
        assertEquals(BrandRequestStatus.PENDING, request.getStatus());
    }

    @Test
    void forgedSupplierCompanyCannotBypassPrivateBrandSelection() {
        UUID foreign = UUID.randomUUID();
        doThrow(new UnauthorizedAccessException("Outro vínculo"))
                .when(rbac)
                .requireOwnCompany(foreign);
        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.selectable(brand.getId(), foreign));
    }

    @Test
    void normalizationConsolidatesSpacingAndCase() {
        assertEquals("solar brand", BrandService.normalize("  SOLAR   Brand  "));
    }
}
