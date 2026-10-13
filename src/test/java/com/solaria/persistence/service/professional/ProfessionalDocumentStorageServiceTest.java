package com.solaria.persistence.service.professional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.professional.ProfessionalDocument;
import com.solaria.persistence.exception.StorageException;
import com.solaria.persistence.service.shared.CloudinaryService;

class ProfessionalDocumentStorageServiceTest {
    private final CloudinaryService cloudinary = mock(CloudinaryService.class);
    private final ProfessionalDocumentStorageService service = new ProfessionalDocumentStorageService(cloudinary, new CloudinaryProperties());
    @Test void validatedEvidenceUsesAuthenticatedStorageWithoutDatabaseBytes() throws Exception {
        var doc = new ProfessionalDocument(); doc.setId(UUID.randomUUID()); doc.setUserId(UUID.randomUUID()); doc.setContent(new byte[]{1}); doc.setContentType("image/png");
        doc.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(doc.getContent())));
        when(cloudinary.uploadAuthenticated(any(), anyString(), anySet(), anyLong())).thenReturn(new CloudinaryService.AuthenticatedUpload("private/document", "png"));
        service.archive(doc);
        assertNull(doc.getContent()); assertEquals("private/document", doc.getCloudinaryPublicId());
        verify(cloudinary).cleanupOnRollback("private/document", true);
    }
    @Test void readsLegacyBytesAndChecksIntegrityForCloudinaryEvidence() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        var doc = new ProfessionalDocument(); doc.setContent(content);
        doc.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content)));
        assertArrayEquals(content, service.read(doc)); verifyNoInteractions(cloudinary);
        doc.setContent(null); doc.setCloudinaryPublicId("private/document"); doc.setCloudinaryFormat("png");
        when(cloudinary.readAuthenticated("private/document", "png", 10 * 1024 * 1024)).thenReturn(content);
        assertArrayEquals(content, service.read(doc));
        when(cloudinary.readAuthenticated("private/document", "png", 10 * 1024 * 1024)).thenReturn(new byte[]{9});
        assertThrows(StorageException.class, () -> service.read(doc));
    }
    @Test void keepsNonImageEvidenceProtectedInDatabaseUntilRawStorageIsSupported() throws Exception {
        byte[] bytes = "%PDF-1.4".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        var doc = new ProfessionalDocument(); doc.setContentType("application/pdf"); doc.setContent(bytes);
        doc.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        service.archive(doc);
        assertArrayEquals(bytes, service.read(doc));
        assertNull(doc.getCloudinaryPublicId());
        verifyNoInteractions(cloudinary);
    }
}
