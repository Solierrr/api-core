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

/**
 * Classe centralizadora que fala diretamente com o SDK do Cloudinary.
 */
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
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

        try {
            // comunicação com cloudnary-> retorna um map com o resultado do upload / upload síncrono
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
            throw new StorageException("Falha ao enviar arquivo para o Cloudinary");
        }
    }

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
        } catch (IOException e) {
            throw new StorageException("Falha ao remover arquivo do Cloudinary");
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
