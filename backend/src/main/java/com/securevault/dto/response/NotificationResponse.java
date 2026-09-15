package com.securevault.dto.response;

import com.securevault.entity.enums.NotificationType;

import java.time.ZonedDateTime;

public class NotificationResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private String referenceId;
    private ZonedDateTime createdAt;

    public NotificationResponse() {}

    public NotificationResponse(Long id, Long userId, String userEmail, NotificationType type, String title, String message, Boolean isRead, String referenceId, ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static NotificationResponseBuilder builder() { return new NotificationResponseBuilder(); }

    public static class NotificationResponseBuilder {
        private Long id;
        private Long userId;
        private String userEmail;
        private NotificationType type;
        private String title;
        private String message;
        private Boolean isRead;
        private String referenceId;
        private ZonedDateTime createdAt;

        public NotificationResponseBuilder id(Long id) { this.id = id; return this; }
        public NotificationResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public NotificationResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public NotificationResponseBuilder type(NotificationType type) { this.type = type; return this; }
        public NotificationResponseBuilder title(String title) { this.title = title; return this; }
        public NotificationResponseBuilder message(String message) { this.message = message; return this; }
        public NotificationResponseBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public NotificationResponseBuilder referenceId(String referenceId) { this.referenceId = referenceId; return this; }
        public NotificationResponseBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public NotificationResponse build() {
            return new NotificationResponse(id, userId, userEmail, type, title, message, isRead, referenceId, createdAt);
        }
    }
}
