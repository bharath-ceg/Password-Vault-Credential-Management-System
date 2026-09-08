package com.securevault.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "suspicious_activities", indexes = {
    @Index(name = "idx_suspicious_user_email", columnList = "user_email"),
    @Index(name = "idx_suspicious_detected_at", columnList = "detected_at")
})
public class SuspiciousActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType; // MULTIPLE_FAILED_LOGINS

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 20)
    private String status; // FLAGGED, RESOLVED, READ

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private ZonedDateTime detectedAt;

    public SuspiciousActivity() {}

    public SuspiciousActivity(Long id, User user, String userEmail, String activityType, String description, String status, Boolean isRead, ZonedDateTime detectedAt) {
        this.id = id;
        this.user = user;
        this.userEmail = userEmail;
        this.activityType = activityType;
        this.description = description;
        this.status = status != null ? status : "FLAGGED";
        this.isRead = isRead != null ? isRead : false;
        this.detectedAt = detectedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.detectedAt == null) {
            this.detectedAt = ZonedDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public ZonedDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(ZonedDateTime detectedAt) { this.detectedAt = detectedAt; }

    public static SuspiciousActivityBuilder builder() { return new SuspiciousActivityBuilder(); }

    public static class SuspiciousActivityBuilder {
        private Long id;
        private User user;
        private String userEmail;
        private String activityType;
        private String description;
        private String status = "FLAGGED";
        private Boolean isRead = false;
        private ZonedDateTime detectedAt;

        public SuspiciousActivityBuilder id(Long id) { this.id = id; return this; }
        public SuspiciousActivityBuilder user(User user) { this.user = user; return this; }
        public SuspiciousActivityBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public SuspiciousActivityBuilder activityType(String activityType) { this.activityType = activityType; return this; }
        public SuspiciousActivityBuilder description(String description) { this.description = description; return this; }
        public SuspiciousActivityBuilder status(String status) { this.status = status; return this; }
        public SuspiciousActivityBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public SuspiciousActivityBuilder detectedAt(ZonedDateTime detectedAt) { this.detectedAt = detectedAt; return this; }

        public SuspiciousActivity build() {
            return new SuspiciousActivity(id, user, userEmail, activityType, description, status, isRead, detectedAt);
        }
    }
}
