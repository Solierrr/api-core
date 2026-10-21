package com.solaria.persistence.service.shared;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.Set;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.solaria.persistence.exception.InvalidFieldException;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class CloudinaryServiceTest {
    private final Cloudinary cloudinary = mock(Cloudinary.class);
    private final Uploader uploader = mock(Uploader.class);
    private final CloudinaryService service = new CloudinaryService(cloudinary, ObservationRegistry.NOOP);
    @AfterEach void cleanup() { if (TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.clearSynchronization(); }
    @Test void authenticatedUploadProtectsOriginalAndDerivedAssets() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), anyMap())).thenReturn(Map.of("public_id", "private/certificate", "format", "png"));
        var result = service.uploadAuthenticated(new MockMultipartFile("file", "c.png", "image/png", new byte[]{1}), "documents", Set.of("image/png"), 1024);
        assertEquals("private/certificate", result.publicId());
        verify(uploader).upload(any(), argThat(options -> "authenticated".equals(options.get("type")) && "image".equals(options.get("resource_type"))));
    }
    @Test void rollbackDeletesPrivateAssetWithCorrectDeliveryType() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        TransactionSynchronizationManager.initSynchronization();
        service.cleanupOnRollback("private/certificate", true);
        verifyNoInteractions(uploader);
        TransactionSynchronizationManager.getSynchronizations().getFirst().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        verify(uploader).destroy(eq("private/certificate"), argThat(options -> "authenticated".equals(options.get("type"))));
    }
    @Test void deleteNeverDestroysSharedMockAssets() throws Exception {
        service.delete("mocks/enterprises-pfp-0a1b2c3d4e5f");
        service.deleteAfterCommit("mocks/pfp-suitman-1a2b3c4d5e6f");
        verifyNoInteractions(cloudinary);
    }
    @Test void deleteStillDestroysRegularAssets() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        service.delete("solaria/usuarios/42/profile");
        verify(uploader).destroy(eq("solaria/usuarios/42/profile"), anyMap());
    }
    @Test void rejectsInvalidFilesBeforeCallingCloudinary() {
        assertThrows(InvalidFieldException.class, () -> service.uploadAuthenticated(new MockMultipartFile("file", "c.pdf", "application/pdf", new byte[]{1}), "documents", Set.of("image/png"), 1024));
        assertThrows(InvalidFieldException.class, () -> service.uploadAuthenticated(new MockMultipartFile("file", "c.png", "image/png", new byte[1025]), "documents", Set.of("image/png"), 1024));
        verifyNoInteractions(cloudinary);
    }
}
