package com.securevault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VaultCredentialRequest {

    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String applicationUrl;

    @Size(max = 150, message = "Alias name cannot exceed 150 characters")
    private String aliasName;

    @NotBlank(message = "Username is required")
    @Size(max = 150, message = "Username cannot exceed 150 characters")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public VaultCredentialRequest() {}

    public VaultCredentialRequest(String applicationUrl, String aliasName, String username, String password) {
        this.applicationUrl = applicationUrl;
        this.aliasName = aliasName;
        this.username = username;
        this.password = password;
    }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
