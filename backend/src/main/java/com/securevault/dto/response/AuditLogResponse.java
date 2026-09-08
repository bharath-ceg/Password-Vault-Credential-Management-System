package com.securevault.dto.response;

import java.time.ZonedDateTime;

public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String description;
    private ZonedDateTime timestamp;

    public AuditLogResponse() {}

    public AuditLogResponse(Long id, Long userId, String userEmail, String action, String description, ZonedDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }

    public static AuditLogResponseBuilder builder() { return new AuditLogResponseBuilder(); }

    public static class AuditLogResponseBuilder {
        private Long id;
        private Long userId;
        private String userEmail;
        private String action;
        private String description;
        private ZonedDateTime timestamp;

        public AuditLogResponseBuilder id(Long id) { this.id = id; return this; }
        public AuditLogResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public AuditLogResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public AuditLogResponseBuilder action(String action) { this.action = action; return this; }
        public AuditLogResponseBuilder description(String description) { this.description = description; return this; }
        public AuditLogResponseBuilder timestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AuditLogResponse build() {
            return new AuditLogResponse(id, userId, userEmail, action, description, timestamp);
        }
    }
}
