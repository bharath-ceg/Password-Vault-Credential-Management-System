package com.securevault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePrivacyPasswordRequest {

    @NotBlank(message = "Account login password is required")
    private String loginPassword;

    @NotBlank(message = "New privacy password is required")
    @Size(min = 8, max = 100, message = "Privacy password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_\\-]).*$", 
             message = "Privacy password must contain at least 8 characters, an uppercase letter, a lowercase letter, a number, and a special character.")
    private String newPrivacyPassword;

    public ChangePrivacyPasswordRequest() {}

    public ChangePrivacyPasswordRequest(String loginPassword, String newPrivacyPassword) {
        this.loginPassword = loginPassword;
        this.newPrivacyPassword = newPrivacyPassword;
    }

    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }

    public String getNewPrivacyPassword() { return newPrivacyPassword; }
    public void setNewPrivacyPassword(String newPrivacyPassword) { this.newPrivacyPassword = newPrivacyPassword; }
}
