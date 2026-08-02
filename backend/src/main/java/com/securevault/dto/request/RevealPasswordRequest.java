package com.securevault.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RevealPasswordRequest {

    @NotBlank(message = "Privacy Password is required")
    private String privacyPassword;

    public RevealPasswordRequest() {}

    public RevealPasswordRequest(String privacyPassword) {
        this.privacyPassword = privacyPassword;
    }

    public String getPrivacyPassword() { return privacyPassword; }
    public void setPrivacyPassword(String privacyPassword) { this.privacyPassword = privacyPassword; }
}
