package com.solaria.persistence.service.professional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.zip.ZipInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class ProfessionalDocumentTypes {
    static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    static void validate(String type, byte[] bytes) {
        boolean valid = switch (type) {
            case "application/pdf" -> bytes.length >= 5 && new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-");
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> isDocx(bytes);
            default -> true; // Existing image validation remains unchanged.
        };
        if (!valid) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document content does not match its format");
    }
    private static boolean isDocx(byte[] bytes) {
        try (var zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            boolean document = false, types = false;
            int entries = 0;
            long expanded = 0;
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++entries > 2000) return false;
                if (entry.getName().equals("word/vbaProject.bin")) return false;
                document |= entry.getName().equals("word/document.xml");
                types |= entry.getName().equals("[Content_Types].xml");
                byte[] buffer = new byte[8192];
                int count;
                while ((count = zip.read(buffer)) != -1) { expanded += count; if (expanded > 50L * 1024 * 1024) return false; }
            }
            return document && types;
        } catch (java.io.IOException exception) { return false; }
    }
    private ProfessionalDocumentTypes() { }
}
