package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.ModelPhoto;
import com.solaria.persistence.domain.enums.catalog.ModelStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.repository.catalog.ModelPhotoRepository;
import com.solaria.persistence.repository.catalog.ModelRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.service.shared.CloudinaryService;

class ModelPhotoServiceTest {
    private final ModelRepository models = mock(ModelRepository.class);
    private final ModelPhotoRepository photos = mock(ModelPhotoRepository.class);
    private final CloudinaryService cloudinary = mock(CloudinaryService.class);
    private final RbacAuthorizationService rbac = mock(RbacAuthorizationService.class);
    private final Model model = new Model();
    private ModelPhotoService service;
    @BeforeEach void setup() {
        model.setId(UUID.randomUUID()); model.setCreatorCompanyId(UUID.randomUUID());
        when(models.lockById(model.getId())).thenReturn(Optional.of(model));
        service = new ModelPhotoService(photos, models, cloudinary, new CloudinaryProperties(), rbac);
    }
    @Test void rejectsChangesToApprovedModelsBeforeUploading() {
        model.setStatus(ModelStatus.APPROVED);
        assertThrows(BusinessRuleException.class, () -> service.upload(model.getId(), new MockMultipartFile("file", "panel.png", "image/png", new byte[]{1})));
        verify(rbac).requireOwnCompany(model.getCreatorCompanyId()); verifyNoInteractions(cloudinary);
    }
    @Test void persistenceFailureCleansUploadedAsset() {
        when(cloudinary.upload(any(), anyString(), isNull(), eq(false), anySet(), anyLong())).thenReturn(new CloudinaryService.UploadResult("https://cdn.test/panel.png", "panel"));
        when(photos.saveAndFlush(any())).thenThrow(new IllegalStateException("database"));
        assertThrows(IllegalStateException.class, () -> service.upload(model.getId(), new MockMultipartFile("file", "panel.png", "image/png", new byte[]{1})));
        verify(cloudinary).cleanupOnRollback("panel", false); verify(cloudinary).delete("panel");
    }
    @Test void primaryUploadDemotesPreviousPhotoBeforeInsertingReplacement() {
        var previous = new ModelPhoto(); previous.setPrimary(true);
        when(photos.findByModelIdAndPrimaryTrue(model.getId())).thenReturn(java.util.List.of(previous));
        when(cloudinary.upload(any(), anyString(), isNull(), eq(false), anySet(), anyLong()))
                .thenReturn(new CloudinaryService.UploadResult("https://cdn.test/main.png", "main"));
        when(photos.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.upload(model.getId(), new MockMultipartFile("file", "main.png", "image/png", new byte[]{1}), true);
        assertTrue(result.isPrimary()); assertFalse(previous.isPrimary());
        var order = inOrder(photos);
        order.verify(photos).findByModelIdAndPrimaryTrue(model.getId());
        order.verify(photos).saveAllAndFlush(java.util.List.of(previous));
        order.verify(photos).saveAndFlush(any(ModelPhoto.class));
    }
    @Test void deletionUsesOwnerAndDefersCloudinaryRemovalUntilCommit() {
        var photo = new ModelPhoto(); photo.setId(UUID.randomUUID()); photo.setModel(model); photo.setPublicId("panel");
        when(photos.findById(photo.getId())).thenReturn(Optional.of(photo));
        service.delete(photo.getId());
        verify(rbac).requireOwnCompany(model.getCreatorCompanyId());
        verify(photos).delete(photo); verify(cloudinary).deleteAfterCommit("panel"); verify(cloudinary, never()).delete(anyString());
    }
}
