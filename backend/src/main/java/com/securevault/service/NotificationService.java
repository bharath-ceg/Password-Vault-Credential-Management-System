package com.securevault.service;

import com.securevault.dto.response.NotificationResponse;
import com.securevault.dto.response.UnreadNotificationCountResponse;
import com.securevault.entity.User;
import com.securevault.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(User user, NotificationType type, String title, String message, String referenceId);

    List<NotificationResponse> getUserNotifications(String userEmail);

    UnreadNotificationCountResponse getUnreadNotificationCount(String userEmail);

    NotificationResponse markNotificationAsRead(String userEmail, Long notificationId);

    void markAllNotificationsAsRead(String userEmail);

    void checkPasswordExpirationNotifications(String userEmail);
}
