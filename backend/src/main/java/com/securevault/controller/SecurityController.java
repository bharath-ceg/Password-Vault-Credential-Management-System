package com.securevault.controller;

import com.securevault.dto.response.ApiResponse;
import com.securevault.dto.response.AuditLogResponse;
import com.securevault.dto.response.LoginLogResponse;
import com.securevault.dto.response.SecurityAlertResponse;
import com.securevault.dto.response.SecurityAnalyticsResponse;
import com.securevault.dto.response.SuspiciousActivityResponse;
import com.securevault.dto.response.UnreadSecurityCountsResponse;
import com.securevault.service.SecurityMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityController {

    private final SecurityMonitoringService securityMonitoringService;

    public SecurityController(SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }

    @GetMapping("/login-activity")
    public ResponseEntity<ApiResponse<List<LoginLogResponse>>> getLoginActivity(Authentication authentication) {
        List<LoginLogResponse> logs = securityMonitoringService.getUserLoginLogs(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Login activity fetched successfully", logs));
    }

    @PostMapping("/login-activity/read-all")
    public ResponseEntity<ApiResponse<String>> markLoginActivityAsRead(Authentication authentication) {
        securityMonitoringService.markLoginActivityAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Login activity marked as read"));
    }

    @GetMapping("/suspicious-activity")
    public ResponseEntity<ApiResponse<List<SuspiciousActivityResponse>>> getSuspiciousActivity(Authentication authentication) {
        List<SuspiciousActivityResponse> activities = securityMonitoringService.getUserSuspiciousActivities(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Suspicious activities fetched successfully", activities));
    }

    @PatchMapping("/suspicious-activity/{id}/read")
    public ResponseEntity<ApiResponse<SuspiciousActivityResponse>> markSuspiciousActivityAsRead(@PathVariable("id") Long id,
                                                                                                 Authentication authentication) {
        SuspiciousActivityResponse activity = securityMonitoringService.markSuspiciousActivityAsRead(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Suspicious activity marked as read", activity));
    }

    @PostMapping("/suspicious-activity/read-all")
    public ResponseEntity<ApiResponse<String>> markAllSuspiciousActivitiesAsRead(Authentication authentication) {
        securityMonitoringService.markAllSuspiciousActivitiesAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("All suspicious activities marked as read"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SecurityAlertResponse>>> getSecurityAlerts(Authentication authentication) {
        List<SecurityAlertResponse> alerts = securityMonitoringService.getUserSecurityAlerts(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Security alerts fetched successfully", alerts));
    }

    @PatchMapping("/alerts/{id}/read")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> markAlertAsRead(@PathVariable("id") Long id,
                                                                               Authentication authentication) {
        SecurityAlertResponse alert = securityMonitoringService.markAlertAsRead(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Security alert marked as read", alert));
    }

    @PutMapping("/alerts/{id}/read")
    public ResponseEntity<ApiResponse<SecurityAlertResponse>> markAlertAsReadPut(@PathVariable("id") Long id,
                                                                                  Authentication authentication) {
        SecurityAlertResponse alert = securityMonitoringService.markAlertAsRead(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Security alert marked as read", alert));
    }

    @PostMapping("/alerts/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAlertsAsRead(Authentication authentication) {
        securityMonitoringService.markAllAlertsAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("All security alerts marked as read"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(Authentication authentication) {
        List<AuditLogResponse> logs = securityMonitoringService.getUserAuditLogs(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", logs));
    }

    @PostMapping("/audit-logs/read-all")
    public ResponseEntity<ApiResponse<String>> markAuditLogsAsRead(Authentication authentication) {
        securityMonitoringService.markAuditLogsAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Audit logs marked as read"));
    }

    @GetMapping("/unread-counts")
    public ResponseEntity<ApiResponse<UnreadSecurityCountsResponse>> getUnreadSecurityCounts(Authentication authentication) {
        UnreadSecurityCountsResponse counts = securityMonitoringService.getUnreadSecurityCounts(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Unread security counts fetched successfully", counts));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<SecurityAnalyticsResponse>> getSecurityAnalytics(Authentication authentication) {
        SecurityAnalyticsResponse analytics = securityMonitoringService.getSecurityAnalytics(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Security analytics fetched successfully", analytics));
    }
}
