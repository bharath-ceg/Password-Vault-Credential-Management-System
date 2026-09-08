package com.securevault.service;

import com.securevault.dto.response.AuditLogResponse;
import com.securevault.dto.response.LoginLogResponse;
import com.securevault.dto.response.SecurityAlertResponse;
import com.securevault.dto.response.SuspiciousActivityResponse;
import com.securevault.dto.response.UnreadSecurityCountsResponse;
import com.securevault.dto.response.SecurityAnalyticsResponse;
import com.securevault.entity.User;

import java.util.List;

public interface SecurityMonitoringService {

    void recordLoginAttempt(User user, String email, boolean success);

    void recordAuditLog(User user, String email, String action, String description);

    List<LoginLogResponse> getUserLoginLogs(String userEmail);

    List<SuspiciousActivityResponse> getUserSuspiciousActivities(String userEmail);

    List<SecurityAlertResponse> getUserSecurityAlerts(String userEmail);

    SecurityAlertResponse markAlertAsRead(String userEmail, Long alertId);

    void markAllAlertsAsRead(String userEmail);

    SuspiciousActivityResponse markSuspiciousActivityAsRead(String userEmail, Long suspiciousId);

    void markAllSuspiciousActivitiesAsRead(String userEmail);

    void markLoginActivityAsRead(String userEmail);

    void markAuditLogsAsRead(String userEmail);

    UnreadSecurityCountsResponse getUnreadSecurityCounts(String userEmail);

    List<AuditLogResponse> getUserAuditLogs(String userEmail);

    SecurityAnalyticsResponse getSecurityAnalytics(String userEmail);
}
