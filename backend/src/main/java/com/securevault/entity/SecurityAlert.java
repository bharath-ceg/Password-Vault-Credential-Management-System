package com.securevault.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "security_alerts", indexes = {
    @Index(name = "idx_alert_user_email", columnList = "user_email"),
    @Index(name = "idx_alert_created_at", columnList = "created_at")
})
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // MULTIPLE_FAILED_LOGIN_ATTEMPTS

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, length = 20)
    private String severity; // HIGH, MEDIUM, LOW

    @Column(nullable = false, length = 20)
    private String status; // UNREAD, READ

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "read_at")
    private ZonedDateTime readAt;

    public SecurityAlert() {}

    public SecurityAlert(Long id, User user, String userEmail, String alertType, String message, String severity, String status, Boolean isRead, ZonedDateTime createdAt, ZonedDateTime readAt) {
        this.id = id;
        this.user = user;
        this.userEmail = userEmail;
        this.alertType = alertType;
        this.message = message;
        this.severity = severity != null ? severity : "HIGH";
        this.status = status != null ? status : "UNREAD";
        this.isRead = isRead != null ? isRead : false;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = ZonedDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        if ("READ".equalsIgnoreCase(status)) {
            this.isRead = true;
            if (this.readAt == null) {
                this.readAt = ZonedDateTime.now();
            }
        }
    }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (Boolean.TRUE.equals(isRead)) {
            this.status = "READ";
            if (this.readAt == null) {
                this.readAt = ZonedDateTime.now();
            }
        }
    }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getReadAt() { return readAt; }
    public void setReadAt(ZonedDateTime readAt) { this.readAt = readAt; }

    public static SecurityAlertBuilder builder() { return new SecurityAlertBuilder(); }

    public static class SecurityAlertBuilder {
        private Long id;
        private User user;
        private String userEmail;
        private String alertType;
        private String message;
        private String severity = "HIGH";
        private String status = "UNREAD";
        private Boolean isRead = false;
        private ZonedDateTime createdAt;
        private ZonedDateTime readAt;

        public SecurityAlertBuilder id(Long id) { this.id = id; return this; }
        public SecurityAlertBuilder user(User user) { this.user = user; return this; }
        public SecurityAlertBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public SecurityAlertBuilder alertType(String alertType) { this.alertType = alertType; return this; }
        public SecurityAlertBuilder message(String message) { this.message = message; return this; }
        public SecurityAlertBuilder severity(String severity) { this.severity = severity; return this; }
        public SecurityAlertBuilder status(String status) { this.status = status; return this; }
        public SecurityAlertBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public SecurityAlertBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SecurityAlertBuilder readAt(ZonedDateTime readAt) { this.readAt = readAt; return this; }

        public SecurityAlert build() {
            return new SecurityAlert(id, user, userEmail, alertType, message, severity, status, isRead, createdAt, readAt);
        }
    }
}
