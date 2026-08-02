package com.securevault.dto.response;

import java.time.ZonedDateTime;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Boolean isEmailVerified;
    private Boolean hasPrivacyPassword;
    private ZonedDateTime createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String fullName, String email, Boolean isEmailVerified, Boolean hasPrivacyPassword, ZonedDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.hasPrivacyPassword = hasPrivacyPassword;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Boolean getHasPrivacyPassword() { return hasPrivacyPassword; }
    public void setHasPrivacyPassword(Boolean hasPrivacyPassword) { this.hasPrivacyPassword = hasPrivacyPassword; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static UserResponseBuilder builder() { return new UserResponseBuilder(); }

    public static class UserResponseBuilder {
        private Long id;
        private String fullName;
        private String email;
        private Boolean isEmailVerified;
        private Boolean hasPrivacyPassword;
        private ZonedDateTime createdAt;

        public UserResponseBuilder id(Long id) { this.id = id; return this; }
        public UserResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder isEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; return this; }
        public UserResponseBuilder hasPrivacyPassword(Boolean hasPrivacyPassword) { this.hasPrivacyPassword = hasPrivacyPassword; return this; }
        public UserResponseBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public UserResponse build() {
            return new UserResponse(id, fullName, email, isEmailVerified, hasPrivacyPassword, createdAt);
        }
    }
}
