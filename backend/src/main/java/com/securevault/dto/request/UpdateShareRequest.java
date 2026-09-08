package com.securevault.dto.request;

import com.securevault.entity.enums.SharePermission;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class UpdateShareRequest {

    @NotNull(message = "Permission level is required")
    private SharePermission permission;

    private ZonedDateTime expiresAt;

    public UpdateShareRequest() {}

    public UpdateShareRequest(SharePermission permission, ZonedDateTime expiresAt) {
        this.permission = permission;
        this.expiresAt = expiresAt;
    }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }
}
