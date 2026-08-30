package com.solaria.persistence.service;

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

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
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
