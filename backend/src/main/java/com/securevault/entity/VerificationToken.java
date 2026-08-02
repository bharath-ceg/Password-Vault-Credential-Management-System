package com.securevault.entity;

import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "email_verification_tokens", indexes = {
    @Index(name = "idx_email_verification_token", columnList = "token")
})
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "expiry_date", nullable = false)
    private ZonedDateTime expiryDate;

    @Column(name = "is_used", nullable = false, columnDefinition = "boolean default false")
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    public VerificationToken() {}

    public VerificationToken(Long id, String token, String email, String fullName, String passwordHash, ZonedDateTime expiryDate, Boolean isUsed, ZonedDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.expiryDate = expiryDate;
        this.isUsed = isUsed != null ? isUsed : false;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(this.expiryDate);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public ZonedDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(ZonedDateTime expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static VerificationTokenBuilder builder() { return new VerificationTokenBuilder(); }

    public static class VerificationTokenBuilder {
        private Long id;
        private String token;
        private String email;
        private String fullName;
        private String passwordHash;
        private ZonedDateTime expiryDate;
        private Boolean isUsed = false;
        private ZonedDateTime createdAt;

        public VerificationTokenBuilder id(Long id) { this.id = id; return this; }
        public VerificationTokenBuilder token(String token) { this.token = token; return this; }
        public VerificationTokenBuilder email(String email) { this.email = email; return this; }
        public VerificationTokenBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public VerificationTokenBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public VerificationTokenBuilder expiryDate(ZonedDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public VerificationTokenBuilder isUsed(Boolean isUsed) { this.isUsed = isUsed; return this; }
        public VerificationTokenBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public VerificationToken build() {
            return new VerificationToken(id, token, email, fullName, passwordHash, expiryDate, isUsed, createdAt);
        }
    }
}
