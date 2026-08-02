package com.securevault.dto.response;

import com.securevault.entity.enums.CredentialCategory;

import java.time.ZonedDateTime;

public class VaultCredentialResponse {
    private Long id;
    private String applicationUrl;
    private String aliasName;
    private String username;
    private String maskedPassword;
    private String revealedPassword; // Only populated when Privacy Password is verified
    private CredentialCategory category;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public VaultCredentialResponse() {}

    public VaultCredentialResponse(Long id, String applicationUrl, String aliasName, String username, String maskedPassword, String revealedPassword, CredentialCategory category, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.applicationUrl = applicationUrl;
        this.aliasName = aliasName;
        this.username = username;
        this.maskedPassword = maskedPassword;
        this.revealedPassword = revealedPassword;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMaskedPassword() { return maskedPassword; }
    public void setMaskedPassword(String maskedPassword) { this.maskedPassword = maskedPassword; }

    public String getRevealedPassword() { return revealedPassword; }
    public void setRevealedPassword(String revealedPassword) { this.revealedPassword = revealedPassword; }

    public CredentialCategory getCategory() { return category; }
    public void setCategory(CredentialCategory category) { this.category = category; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static VaultCredentialResponseBuilder builder() { return new VaultCredentialResponseBuilder(); }

    public static class VaultCredentialResponseBuilder {
        private Long id;
        private String applicationUrl;
        private String aliasName;
        private String username;
        private String maskedPassword;
        private String revealedPassword;
        private CredentialCategory category;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public VaultCredentialResponseBuilder id(Long id) { this.id = id; return this; }
        public VaultCredentialResponseBuilder applicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; return this; }
        public VaultCredentialResponseBuilder aliasName(String aliasName) { this.aliasName = aliasName; return this; }
        public VaultCredentialResponseBuilder username(String username) { this.username = username; return this; }
        public VaultCredentialResponseBuilder maskedPassword(String maskedPassword) { this.maskedPassword = maskedPassword; return this; }
        public VaultCredentialResponseBuilder revealedPassword(String revealedPassword) { this.revealedPassword = revealedPassword; return this; }
        public VaultCredentialResponseBuilder category(CredentialCategory category) { this.category = category; return this; }
        public VaultCredentialResponseBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public VaultCredentialResponseBuilder updatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public VaultCredentialResponse build() {
            return new VaultCredentialResponse(id, applicationUrl, aliasName, username, maskedPassword, revealedPassword, category, createdAt, updatedAt);
        }
    }
}
