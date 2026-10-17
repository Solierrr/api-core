package com.solaria.persistence.service.shared;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.StorageException;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * Classe centralizadora que fala diretamente com o SDK do Cloudinary.
 *
 * <p>
 * As chamadas ao SDK sao envolvidas em uma {@link Observation} para render um
 * span filho com latencia/erro dessa dependencia externa no trace da
 * requisicao.
 * </p>
 */
@Service
public class CloudinaryService {

    // assets do seed (mocks-warehouse) sao compartilhados entre varias linhas; apagar uma foto nunca deve destruir o asset
    static final String SHARED_MOCK_PREFIX = "mocks/";

    private final Cloudinary cloudinary;
    // registry usado para criar o span das chamadas ao Cloudinary
    private final ObservationRegistry observationRegistry;

    public CloudinaryService(Cloudinary cloudinary, ObservationRegistry observationRegistry) {
        this.cloudinary = cloudinary;
        this.observationRegistry = observationRegistry;
    }

    // dto simples -> representa o resultado do upload
    public record UploadResult(String url, String publicId) {
    }

    /**
     * publicId != null + overwrite=true: upsert determinístico (usado por foto de
     * perfil/banner
     * de usuário/empresa e pela foto única de conta de energia -- o próprio
     * Cloudinary substitui
     * o asset atomicamente, sem precisar de um destroy() separado do asset antigo).
     * publicId == null: Cloudinary gera um id novo (usado por unidade local/model,
     * fotos avulsas).
     */
    public UploadResult upload(
            MultipartFile file, // arquivo enviado via http
            String folder, // pasta do cloudnary
            String publicId, // identificador do asset no cloudnary
            boolean overwrite, // decide se o arquivo pode ter sobrescrita
            Set<String> allowedContentTypes, // conjunto de arquivos permitidos(.jpg / .png)
            long maxBytes) // tamanho maxímo de arquivo permitido
    {

        // validação (vazio/tipo/tamanho) antes de chamar o SDK do cloudnary
        validate(file, allowedContentTypes, maxBytes);

        // map das configs do cloudnary
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "auto",
                "overwrite", overwrite,
                "invalidate", overwrite);

        // adiciona publicId para fotos novas / fotos alteradas permanecem com publicId antigo
        if (publicId != null) {
            options.put("public_id", publicId);
        }


        // comunicação com cloudnary + observation(logging)-> retorna um map com o resultado do upload / upload síncrono
        Observation observation = Observation.createNotStarted("cloudinary.upload", observationRegistry)
                .lowCardinalityKeyValue("cloudinary.folder", folder) // pasta do cloudinary para traces
                .start();

        try (Observation.Scope ignored = observation.openScope()) {
            // comunicação com cloudnary-> retorna um map com o resultado do upload

            Map<?, ?> result = cloudinary // sdk que conversa com o cloudnary
                    .uploader()
                    .upload(
                            file.getBytes(), // converte o arquivo para byte -> sdk recebe dados nesse formato
                            options // configs do cloudnary
                    );

            // secure_url -> url pública do asset | public_id -> id interno -> usado em delete/sobrescrita
            return new UploadResult((String) result.get("secure_url"), (String) result.get("public_id"));
        } catch (IOException e) {
            // qualquer exception vira storageException e retornado como 502
            observation.error(e);
            throw new StorageException("Falha ao enviar arquivo para o Cloudinary");
        } finally {
            observation.stop();
        }
    }

    public record AuthenticatedUpload(String publicId, String format) {}

    public AuthenticatedUpload uploadAuthenticated(MultipartFile file, String folder,
            Set<String> allowedContentTypes, long maxBytes) {
        validate(file, allowedContentTypes, maxBytes);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder, "resource_type", "image", "type", "authenticated", "overwrite", false));
            String publicId = (String) result.get("public_id");
            String format = (String) result.get("format");
            if (publicId == null || publicId.isBlank() || format == null || format.isBlank()) {
                throw new StorageException("Invalid Cloudinary upload response");
            }
            return new AuthenticatedUpload(publicId, format);
        } catch (IOException exception) {
            throw new StorageException("Could not upload authenticated document");
        }
    }

    public byte[] readAuthenticated(String publicId, String format, int maxBytes) {
        try {
            String signedUrl = cloudinary.privateDownload(publicId, format, ObjectUtils.asMap(
                    "type", "authenticated", "resource_type", "image",
                    "expires_at", java.time.Instant.now().plusSeconds(60).getEpochSecond()));
            var connection = (java.net.HttpURLConnection) java.net.URI.create(signedUrl).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(false);
            try {
                if (connection.getResponseCode() != 200) throw new StorageException("Could not read authenticated document");
                try (var stream = connection.getInputStream()) {
                    byte[] bytes = stream.readNBytes(maxBytes + 1);
                    if (bytes.length > maxBytes) throw new StorageException("Document exceeds allowed size");
                    return bytes;
                }
            } finally {
                connection.disconnect();
            }
        } catch (StorageException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new StorageException("Could not read authenticated document");
        }
    }

    public void cleanupOnRollback(String publicId, boolean authenticated) {
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) return;
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) cleanup(publicId, authenticated);
                    }
                });
    }

    public void deleteAfterCommit(String publicId) {
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            delete(publicId);
            return;
        }
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() { cleanup(publicId, false); }
                });
    }

    private void cleanup(String publicId, boolean authenticated) {
        try {
            if (authenticated) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true, "type", "authenticated", "resource_type", "image"));
            } else {
                delete(publicId);
            }
        } catch (Exception exception) {
            org.slf4j.LoggerFactory.getLogger(CloudinaryService.class).error("Cloudinary asset cleanup failed");
        }
    }

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank() || isSharedMockAsset(publicId)) {
            return;
        }

        // destroy sincrono, envolvido numa Observation -> span Sem tags de id
        Observation observation = Observation.createNotStarted("cloudinary.delete", observationRegistry).start();
        try (Observation.Scope ignored = observation.openScope()) {

            cloudinary.uploader()
            .destroy(publicId,
             ObjectUtils.asMap("invalidate", true)
             );

        } catch (IOException e) {
            observation.error(e);
            throw new StorageException("Falha ao remover arquivo do Cloudinary");
        } finally {
            observation.stop();
        }
    }

    public record ImageInfo(int width, int height) {
    }

    /** Dimensoes do asset original, usadas para validar que o recorte cabe na imagem. */
    public ImageInfo imageInfo(String publicId) {
        Observation observation = Observation.createNotStarted("cloudinary.resource", observationRegistry).start();
        try (Observation.Scope ignored = observation.openScope()) {
            Map<?, ?> resource = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            Object width = resource.get("width");
            Object height = resource.get("height");
            if (!(width instanceof Number) || !(height instanceof Number)) {
                throw new StorageException("Resposta inválida do Cloudinary ao ler a imagem");
            }
            return new ImageInfo(((Number) width).intValue(), ((Number) height).intValue());
        } catch (StorageException e) {
            observation.error(e);
            throw e;
        } catch (Exception e) {
            observation.error(e);
            throw new StorageException("Falha ao consultar a imagem no Cloudinary");
        } finally {
            observation.stop();
        }
    }

    /**
     * Grava o recorte como custom_coordinates do asset. Assets compartilhados do seed (mocks/) nao sao alterados,
     * porque varias linhas podem apontar para o mesmo asset; o recorte deles vive so no banco e nas URLs.
     */
    public void setCustomCoordinates(String publicId, int x, int y, int width, int height) {
        if (publicId == null || publicId.isBlank() || isSharedMockAsset(publicId)) {
            return;
        }
        Observation observation = Observation.createNotStarted("cloudinary.explicit", observationRegistry).start();
        try (Observation.Scope ignored = observation.openScope()) {
            cloudinary.uploader().explicit(publicId, ObjectUtils.asMap(
                    "type", "upload",
                    "custom_coordinates", x + "," + y + "," + width + "," + height,
                    "invalidate", true));
        } catch (IOException e) {
            observation.error(e);
            throw new StorageException("Falha ao salvar o recorte no Cloudinary");
        } finally {
            observation.stop();
        }
    }

    static boolean isSharedMockAsset(String publicId) {
        return publicId.startsWith(SHARED_MOCK_PREFIX);
    }

    // método auxiliar de validação
    private void validate(MultipartFile file, Set<String> allowedContentTypes, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFieldException("Arquivo enviado está vazio");
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidFieldException("Arquivo excede o tamanho máximo permitido: " + maxBytes + " bytes");
        }

        // cada classe service decide os tipos de arquivo permitidos
        if (!allowedContentTypes.contains(file.getContentType())) {
            throw new InvalidFieldException("Tipo de arquivo não permitido: " + file.getContentType());
        }
    }
}
