package com.securevault.entity;

import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "password_reset_otps", indexes = {
    @Index(name = "idx_otp_user_code", columnList = "user_id, otp_code")
})
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "expiry_time", nullable = false)
    private ZonedDateTime expiryTime;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    public PasswordResetOtp() {}

    public PasswordResetOtp(Long id, User user, String otpCode, ZonedDateTime expiryTime, Boolean isUsed, ZonedDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.otpCode = otpCode;
        this.expiryTime = expiryTime;
        this.isUsed = isUsed != null ? isUsed : false;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(this.expiryTime);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public ZonedDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(ZonedDateTime expiryTime) { this.expiryTime = expiryTime; }

    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static PasswordResetOtpBuilder builder() { return new PasswordResetOtpBuilder(); }

    public static class PasswordResetOtpBuilder {
        private Long id;
        private User user;
        private String otpCode;
        private ZonedDateTime expiryTime;
        private Boolean isUsed = false;
        private ZonedDateTime createdAt;

        public PasswordResetOtpBuilder id(Long id) { this.id = id; return this; }
        public PasswordResetOtpBuilder user(User user) { this.user = user; return this; }
        public PasswordResetOtpBuilder otpCode(String otpCode) { this.otpCode = otpCode; return this; }
        public PasswordResetOtpBuilder expiryTime(ZonedDateTime expiryTime) { this.expiryTime = expiryTime; return this; }
        public PasswordResetOtpBuilder isUsed(Boolean isUsed) { this.isUsed = isUsed; return this; }
        public PasswordResetOtpBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public PasswordResetOtp build() {
            return new PasswordResetOtp(id, user, otpCode, expiryTime, isUsed, createdAt);
        }
    }
}
