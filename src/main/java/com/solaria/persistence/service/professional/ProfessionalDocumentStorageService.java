package com.solaria.persistence.service.professional;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.professional.ProfessionalDocument;
import com.solaria.persistence.exception.StorageException;
import com.solaria.persistence.service.shared.CloudinaryService;

@Service
public class ProfessionalDocumentStorageService {
    private final CloudinaryService cloudinary;
    private final CloudinaryProperties properties;

    public ProfessionalDocumentStorageService(CloudinaryService cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    public void archive(ProfessionalDocument document) {
        if (document.getCloudinaryPublicId() != null || !document.getContentType().startsWith("image/")) return;
        byte[] bytes = read(document);
        MultipartFile file = new MultipartFile() {
            public String getName() { return "file"; }
            public String getOriginalFilename() { return document.getId().toString(); }
            public String getContentType() { return document.getContentType(); }
            public boolean isEmpty() { return bytes.length == 0; }
            public long getSize() { return bytes.length; }
            public byte[] getBytes() { return bytes; }
            public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(bytes); }
            public void transferTo(java.io.File destination) throws java.io.IOException { java.nio.file.Files.write(destination.toPath(), bytes); }
        };
        var uploaded = cloudinary.uploadAuthenticated(file,
                properties.getFolderPrefix() + "/professionals/" + document.getUserId() + "/documents",
                Set.of("image/jpeg", "image/png", "image/webp"), 10L * 1024 * 1024);
        cloudinary.cleanupOnRollback(uploaded.publicId(), true);
        document.setCloudinaryPublicId(uploaded.publicId());
        document.setCloudinaryFormat(uploaded.format());
        document.setContent(null);
    }

    public byte[] read(ProfessionalDocument document) {
        byte[] bytes = document.getCloudinaryPublicId() == null ? document.getContent()
                : cloudinary.readAuthenticated(document.getCloudinaryPublicId(), document.getCloudinaryFormat(), 10 * 1024 * 1024);
        if (bytes == null) throw new StorageException("Document content is unavailable");
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            if (!hash.equals(document.getSha256())) throw new StorageException("Document integrity check failed");
            return bytes;
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
