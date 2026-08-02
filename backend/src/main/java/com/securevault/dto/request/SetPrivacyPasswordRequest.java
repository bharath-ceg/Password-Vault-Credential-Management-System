package com.securevault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SetPrivacyPasswordRequest {

    @NotBlank(message = "Privacy password is required")
    @Size(min = 8, max = 100, message = "Privacy password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_\\-]).*$", 
             message = "Privacy password must contain at least 8 characters, an uppercase letter, a lowercase letter, a number, and a special character.")
    private String privacyPassword;

    public SetPrivacyPasswordRequest() {}

    public SetPrivacyPasswordRequest(String privacyPassword) {
        this.privacyPassword = privacyPassword;
    }

    public String getPrivacyPassword() { return privacyPassword; }
    public void setPrivacyPassword(String privacyPassword) { this.privacyPassword = privacyPassword; }
}
