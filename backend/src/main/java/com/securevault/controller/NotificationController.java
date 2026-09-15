package com.securevault.controller;

import com.securevault.dto.response.ApiResponse;
import com.securevault.dto.response.NotificationResponse;
import com.securevault.dto.response.UnreadNotificationCountResponse;
import com.securevault.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(Authentication authentication) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadNotificationCountResponse>> getUnreadNotificationCount(Authentication authentication) {
        UnreadNotificationCountResponse countResponse = notificationService.getUnreadNotificationCount(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Unread notification count retrieved successfully", countResponse));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markNotificationAsReadPatch(@PathVariable("id") Long id,
                                                                                           Authentication authentication) {
        NotificationResponse notification = notificationService.markNotificationAsRead(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markNotificationAsReadPut(@PathVariable("id") Long id,
                                                                                         Authentication authentication) {
        NotificationResponse notification = notificationService.markNotificationAsRead(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllNotificationsAsRead(Authentication authentication) {
        notificationService.markAllNotificationsAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
