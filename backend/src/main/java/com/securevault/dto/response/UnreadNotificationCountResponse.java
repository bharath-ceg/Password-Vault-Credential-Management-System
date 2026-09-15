package com.securevault.dto.response;

public class UnreadNotificationCountResponse {

    private long unreadCount;

    public UnreadNotificationCountResponse() {}

    public UnreadNotificationCountResponse(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public static UnreadNotificationCountResponseBuilder builder() { return new UnreadNotificationCountResponseBuilder(); }

    public static class UnreadNotificationCountResponseBuilder {
        private long unreadCount;

        public UnreadNotificationCountResponseBuilder unreadCount(long unreadCount) { this.unreadCount = unreadCount; return this; }

        public UnreadNotificationCountResponse build() {
            return new UnreadNotificationCountResponse(unreadCount);
        }
    }
}
