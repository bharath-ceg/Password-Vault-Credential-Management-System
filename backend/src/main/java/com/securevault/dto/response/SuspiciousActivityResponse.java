package com.securevault.dto.response;

import java.time.ZonedDateTime;

public class SuspiciousActivityResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String activityType;
    private String description;
    private String status;
    private ZonedDateTime detectedAt;

    public SuspiciousActivityResponse() {}

    public SuspiciousActivityResponse(Long id, Long userId, String userEmail, String activityType, String description, String status, ZonedDateTime detectedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.activityType = activityType;
        this.description = description;
        this.status = status;
        this.detectedAt = detectedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ZonedDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(ZonedDateTime detectedAt) { this.detectedAt = detectedAt; }

    public static SuspiciousActivityResponseBuilder builder() { return new SuspiciousActivityResponseBuilder(); }

    public static class SuspiciousActivityResponseBuilder {
        private Long id;
        private Long userId;
        private String userEmail;
        private String activityType;
        private String description;
        private String status;
        private ZonedDateTime detectedAt;

        public SuspiciousActivityResponseBuilder id(Long id) { this.id = id; return this; }
        public SuspiciousActivityResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public SuspiciousActivityResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public SuspiciousActivityResponseBuilder activityType(String activityType) { this.activityType = activityType; return this; }
        public SuspiciousActivityResponseBuilder description(String description) { this.description = description; return this; }
        public SuspiciousActivityResponseBuilder status(String status) { this.status = status; return this; }
        public SuspiciousActivityResponseBuilder detectedAt(ZonedDateTime detectedAt) { this.detectedAt = detectedAt; return this; }

        public SuspiciousActivityResponse build() {
            return new SuspiciousActivityResponse(id, userId, userEmail, activityType, description, status, detectedAt);
        }
    }
}
