package com.securevault.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_email", columnList = "user_email"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 500)
    private String description;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime timestamp;

    public AuditLog() {}

    public AuditLog(Long id, User user, String userEmail, String action, String description, Boolean isRead, ZonedDateTime timestamp) {
        this.id = id;
        this.user = user;
        this.userEmail = userEmail;
        this.action = action;
        this.description = description;
        this.isRead = isRead != null ? isRead : false;
        this.timestamp = timestamp;
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = ZonedDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }

    public static AuditLogBuilder builder() { return new AuditLogBuilder(); }

    public static class AuditLogBuilder {
        private Long id;
        private User user;
        private String userEmail;
        private String action;
        private String description;
        private Boolean isRead = false;
        private ZonedDateTime timestamp;

        public AuditLogBuilder id(Long id) { this.id = id; return this; }
        public AuditLogBuilder user(User user) { this.user = user; return this; }
        public AuditLogBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public AuditLogBuilder action(String action) { this.action = action; return this; }
        public AuditLogBuilder description(String description) { this.description = description; return this; }
        public AuditLogBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public AuditLogBuilder timestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AuditLog build() {
            return new AuditLog(id, user, userEmail, action, description, isRead, timestamp);
        }
    }
}
