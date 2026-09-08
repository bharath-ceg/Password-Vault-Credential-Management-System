package com.securevault.dto.request;

import com.securevault.entity.enums.SharePermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class CreateShareRequest {

    @NotNull(message = "Credential ID is required")
    private Long credentialId;

    @NotNull(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    @NotNull(message = "Permission level is required")
    private SharePermission permission = SharePermission.VIEW_ONLY;

    private ZonedDateTime expiresAt;

    public CreateShareRequest() {}

    public CreateShareRequest(Long credentialId, String recipientEmail, SharePermission permission, ZonedDateTime expiresAt) {
        this.credentialId = credentialId;
        this.recipientEmail = recipientEmail;
        this.permission = permission != null ? permission : SharePermission.VIEW_ONLY;
        this.expiresAt = expiresAt;
    }

    public Long getCredentialId() { return credentialId; }
    public void setCredentialId(Long credentialId) { this.credentialId = credentialId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }
}
