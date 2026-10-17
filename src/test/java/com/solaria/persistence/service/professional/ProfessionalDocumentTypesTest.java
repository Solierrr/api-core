package com.solaria.persistence.service.professional;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
class ProfessionalDocumentTypesTest {
 private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
 private byte[] docx(boolean document, boolean macro) throws Exception {
  var bytes = new ByteArrayOutputStream();
  try(var zip = new ZipOutputStream(bytes)) {
   zip.putNextEntry(new ZipEntry("[Content_Types].xml")); zip.write("<Types/>".getBytes()); zip.closeEntry();
   if(document) { zip.putNextEntry(new ZipEntry("word/document.xml")); zip.write("<document/>".getBytes()); zip.closeEntry(); }
   if(macro) { zip.putNextEntry(new ZipEntry("word/vbaProject.bin")); zip.write(new byte[]{1}); zip.closeEntry(); }
  }
  return bytes.toByteArray();
 }
 @Test void checksPdfSignature() {
  assertDoesNotThrow(() -> ProfessionalDocumentTypes.validate("application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)));
  assertThrows(ResponseStatusException.class, () -> ProfessionalDocumentTypes.validate("application/pdf", "not a pdf".getBytes()));
 }
 @Test void requiresOpenXmlDocumentAndRejectsMacrosAndArbitraryZip() throws Exception {
  assertDoesNotThrow(() -> ProfessionalDocumentTypes.validate(DOCX, docx(true,false)));
  assertThrows(ResponseStatusException.class, () -> ProfessionalDocumentTypes.validate(DOCX, docx(false,false)));
  assertThrows(ResponseStatusException.class, () -> ProfessionalDocumentTypes.validate(DOCX, docx(true,true)));
  assertThrows(ResponseStatusException.class, () -> ProfessionalDocumentTypes.validate(DOCX, new byte[]{1,2,3}));
 }
 @Test void rejectsExcessiveZipExpansion() throws Exception {
  var bytes = new ByteArrayOutputStream();
  try(var zip = new ZipOutputStream(bytes)) {
   zip.putNextEntry(new ZipEntry("word/document.xml"));
   byte[] block = new byte[1024*1024];
   for(int i=0;i<51;i++) zip.write(block);
   zip.closeEntry();
  }
  assertThrows(ResponseStatusException.class, () -> ProfessionalDocumentTypes.validate(DOCX,bytes.toByteArray()));
 }
}
