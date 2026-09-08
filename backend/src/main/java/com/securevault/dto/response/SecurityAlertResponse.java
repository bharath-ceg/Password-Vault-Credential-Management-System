package com.securevault.dto.response;

import java.time.ZonedDateTime;

public class SecurityAlertResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String alertType;
    private String message;
    private String severity;
    private String status;
    private ZonedDateTime createdAt;
    private ZonedDateTime readAt;

    public SecurityAlertResponse() {}

    public SecurityAlertResponse(Long id, Long userId, String userEmail, String alertType, String message, String severity, String status, ZonedDateTime createdAt, ZonedDateTime readAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.alertType = alertType;
        this.message = message;
        this.severity = severity;
        this.status = status;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getReadAt() { return readAt; }
    public void setReadAt(ZonedDateTime readAt) { this.readAt = readAt; }

    public static SecurityAlertResponseBuilder builder() { return new SecurityAlertResponseBuilder(); }

    public static class SecurityAlertResponseBuilder {
        private Long id;
        private Long userId;
        private String userEmail;
        private String alertType;
        private String message;
        private String severity;
        private String status;
        private ZonedDateTime createdAt;
        private ZonedDateTime readAt;

        public SecurityAlertResponseBuilder id(Long id) { this.id = id; return this; }
        public SecurityAlertResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public SecurityAlertResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public SecurityAlertResponseBuilder alertType(String alertType) { this.alertType = alertType; return this; }
        public SecurityAlertResponseBuilder message(String message) { this.message = message; return this; }
        public SecurityAlertResponseBuilder severity(String severity) { this.severity = severity; return this; }
        public SecurityAlertResponseBuilder status(String status) { this.status = status; return this; }
        public SecurityAlertResponseBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SecurityAlertResponseBuilder readAt(ZonedDateTime readAt) { this.readAt = readAt; return this; }

        public SecurityAlertResponse build() {
            return new SecurityAlertResponse(id, userId, userEmail, alertType, message, severity, status, createdAt, readAt);
        }
    }
}
