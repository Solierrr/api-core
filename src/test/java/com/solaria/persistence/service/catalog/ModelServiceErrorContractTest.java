package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.dto.request.catalog.ModelRequestDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.repository.catalog.InventoryRepository;
import com.solaria.persistence.repository.catalog.ModelPhotoRepository;
import com.solaria.persistence.repository.catalog.ModelRepository;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

class ModelServiceErrorContractTest {
    private final ModelRepository models = mock(ModelRepository.class);
    private final ModelService service = new ModelService(models, mock(InventoryRepository.class),
            mock(OfferRepository.class), mock(ModelPhotoRepository.class),
            mock(RbacAuthorizationService.class), mock(BrandService.class));

    private Model approvedModel() {
        Model model = new Model();
        model.setId(UUID.randomUUID());
        model.setCreatorCompanyId(UUID.randomUUID());
        model.setStatus(ModelStatus.APPROVED);
        when(models.lockById(model.getId())).thenReturn(Optional.of(model));
        return model;
    }

    @Test
    void editingAnApprovedModelReturnsASpecificCodeWithoutSaving() {
        Model model = approvedModel();
        var error = assertThrows(BusinessRuleException.class,
                () -> service.update(model.getId(), new ModelRequestDTO()));
        assertEquals("MODEL_NOT_EDITABLE", error.getErrorCode());
        verify(models, never()).save(any());
    }

    @Test
    void deletingAnApprovedModelReturnsASpecificCodeWithoutDeleting() {
        Model model = approvedModel();
        var error = assertThrows(BusinessRuleException.class, () -> service.deleteById(model.getId()));
        assertEquals("MODEL_NOT_REMOVABLE", error.getErrorCode());
        verify(models, never()).deleteById(any());
    }

    @Test
    void approvingOrRejectingAnApprovedModelReturnsTheSameTransitionCode() {
        Model model = approvedModel();
        assertEquals("MODEL_INVALID_STATUS_TRANSITION",
                assertThrows(BusinessRuleException.class, () -> service.approve(model.getId())).getErrorCode());
        assertEquals("MODEL_INVALID_STATUS_TRANSITION",
                assertThrows(BusinessRuleException.class, () -> service.reject(model.getId())).getErrorCode());
        verify(models, never()).save(any());
    }
}
