package com.solaria.persistence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciais e configuração de organização de pastas do Cloudinary.
 */

// Habilita essa classe com as variaveis em application.properties com prefixo cloudinary
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String cloudName;

    private String apiKey;

    private String apiSecret;

    private String folderPrefix = "solier";

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getFolderPrefix() {
        return folderPrefix;
    }

    public void setFolderPrefix(String folderPrefix) {
        this.folderPrefix = folderPrefix;
    }
}
