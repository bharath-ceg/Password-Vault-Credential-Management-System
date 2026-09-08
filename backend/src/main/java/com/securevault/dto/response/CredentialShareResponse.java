package com.securevault.dto.response;

import com.securevault.entity.enums.CredentialCategory;
import com.securevault.entity.enums.SharePermission;

import java.time.ZonedDateTime;

public class CredentialShareResponse {

    private Long id;
    private Long credentialId;
    private String credentialAlias;
    private String credentialUrl;
    private String username;
    private CredentialCategory category;
    private String ownerEmail;
    private String ownerFullName;
    private String recipientEmail;
    private String recipientFullName;
    private SharePermission permission;
    private ZonedDateTime createdAt;
    private ZonedDateTime expiresAt;
    private Boolean isExpired;
    private String maskedPassword = "••••••••••••";

    public CredentialShareResponse() {}

    public CredentialShareResponse(Long id, Long credentialId, String credentialAlias, String credentialUrl, String username, CredentialCategory category, String ownerEmail, String ownerFullName, String recipientEmail, String recipientFullName, SharePermission permission, ZonedDateTime createdAt, ZonedDateTime expiresAt, Boolean isExpired) {
        this.id = id;
        this.credentialId = credentialId;
        this.credentialAlias = credentialAlias;
        this.credentialUrl = credentialUrl;
        this.username = username;
        this.category = category;
        this.ownerEmail = ownerEmail;
        this.ownerFullName = ownerFullName;
        this.recipientEmail = recipientEmail;
        this.recipientFullName = recipientFullName;
        this.permission = permission;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isExpired = isExpired != null ? isExpired : false;
        this.maskedPassword = "••••••••••••";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCredentialId() { return credentialId; }
    public void setCredentialId(Long credentialId) { this.credentialId = credentialId; }

    public String getCredentialAlias() { return credentialAlias; }
    public void setCredentialAlias(String credentialAlias) { this.credentialAlias = credentialAlias; }

    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public CredentialCategory getCategory() { return category; }
    public void setCategory(CredentialCategory category) { this.category = category; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getOwnerFullName() { return ownerFullName; }
    public void setOwnerFullName(String ownerFullName) { this.ownerFullName = ownerFullName; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientFullName() { return recipientFullName; }
    public void setRecipientFullName(String recipientFullName) { this.recipientFullName = recipientFullName; }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public String getMaskedPassword() { return maskedPassword; }
    public void setMaskedPassword(String maskedPassword) { this.maskedPassword = maskedPassword; }

    public static CredentialShareResponseBuilder builder() { return new CredentialShareResponseBuilder(); }

    public static class CredentialShareResponseBuilder {
        private Long id;
        private Long credentialId;
        private String credentialAlias;
        private String credentialUrl;
        private String username;
        private CredentialCategory category;
        private String ownerEmail;
        private String ownerFullName;
        private String recipientEmail;
        private String recipientFullName;
        private SharePermission permission;
        private ZonedDateTime createdAt;
        private ZonedDateTime expiresAt;
        private Boolean isExpired = false;

        public CredentialShareResponseBuilder id(Long id) { this.id = id; return this; }
        public CredentialShareResponseBuilder credentialId(Long credentialId) { this.credentialId = credentialId; return this; }
        public CredentialShareResponseBuilder credentialAlias(String credentialAlias) { this.credentialAlias = credentialAlias; return this; }
        public CredentialShareResponseBuilder credentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; return this; }
        public CredentialShareResponseBuilder username(String username) { this.username = username; return this; }
        public CredentialShareResponseBuilder category(CredentialCategory category) { this.category = category; return this; }
        public CredentialShareResponseBuilder ownerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; return this; }
        public CredentialShareResponseBuilder ownerFullName(String ownerFullName) { this.ownerFullName = ownerFullName; return this; }
        public CredentialShareResponseBuilder recipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; return this; }
        public CredentialShareResponseBuilder recipientFullName(String recipientFullName) { this.recipientFullName = recipientFullName; return this; }
        public CredentialShareResponseBuilder permission(SharePermission permission) { this.permission = permission; return this; }
        public CredentialShareResponseBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CredentialShareResponseBuilder expiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public CredentialShareResponseBuilder isExpired(Boolean isExpired) { this.isExpired = isExpired; return this; }

        public CredentialShareResponse build() {
            return new CredentialShareResponse(id, credentialId, credentialAlias, credentialUrl, username, category, ownerEmail, ownerFullName, recipientEmail, recipientFullName, permission, createdAt, expiresAt, isExpired);
        }
    }
}
