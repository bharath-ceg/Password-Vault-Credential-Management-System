package com.securevault.dto.response;

import java.util.List;

public class SecurityAnalyticsResponse {

    private long totalLoginAttempts;
    private long successfulLogins;
    private long failedLogins;
    private long suspiciousActivities;
    private long securityAlerts;
    private List<AuditLogResponse> recentActivities;
    private List<LoginLogResponse> loginLogs;
    private List<SuspiciousActivityResponse> suspiciousActivityLogs;
    private List<SecurityAlertResponse> securityAlertLogs;

    public SecurityAnalyticsResponse() {}

    public SecurityAnalyticsResponse(long totalLoginAttempts, long successfulLogins, long failedLogins,
                                     long suspiciousActivities, long securityAlerts,
                                     List<AuditLogResponse> recentActivities, List<LoginLogResponse> loginLogs,
                                     List<SuspiciousActivityResponse> suspiciousActivityLogs,
                                     List<SecurityAlertResponse> securityAlertLogs) {
        this.totalLoginAttempts = totalLoginAttempts;
        this.successfulLogins = successfulLogins;
        this.failedLogins = failedLogins;
        this.suspiciousActivities = suspiciousActivities;
        this.securityAlerts = securityAlerts;
        this.recentActivities = recentActivities;
        this.loginLogs = loginLogs;
        this.suspiciousActivityLogs = suspiciousActivityLogs;
        this.securityAlertLogs = securityAlertLogs;
    }

    public long getTotalLoginAttempts() { return totalLoginAttempts; }
    public void setTotalLoginAttempts(long totalLoginAttempts) { this.totalLoginAttempts = totalLoginAttempts; }

    public long getSuccessfulLogins() { return successfulLogins; }
    public void setSuccessfulLogins(long successfulLogins) { this.successfulLogins = successfulLogins; }

    public long getFailedLogins() { return failedLogins; }
    public void setFailedLogins(long failedLogins) { this.failedLogins = failedLogins; }

    public long getSuspiciousActivities() { return suspiciousActivities; }
    public void setSuspiciousActivities(long suspiciousActivities) { this.suspiciousActivities = suspiciousActivities; }

    public long getSecurityAlerts() { return securityAlerts; }
    public void setSecurityAlerts(long securityAlerts) { this.securityAlerts = securityAlerts; }

    public List<AuditLogResponse> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<AuditLogResponse> recentActivities) { this.recentActivities = recentActivities; }

    public List<LoginLogResponse> getLoginLogs() { return loginLogs; }
    public void setLoginLogs(List<LoginLogResponse> loginLogs) { this.loginLogs = loginLogs; }

    public List<SuspiciousActivityResponse> getSuspiciousActivityLogs() { return suspiciousActivityLogs; }
    public void setSuspiciousActivityLogs(List<SuspiciousActivityResponse> suspiciousActivityLogs) { this.suspiciousActivityLogs = suspiciousActivityLogs; }

    public List<SecurityAlertResponse> getSecurityAlertLogs() { return securityAlertLogs; }
    public void setSecurityAlertLogs(List<SecurityAlertResponse> securityAlertLogs) { this.securityAlertLogs = securityAlertLogs; }

    public static SecurityAnalyticsResponseBuilder builder() { return new SecurityAnalyticsResponseBuilder(); }

    public static class SecurityAnalyticsResponseBuilder {
        private long totalLoginAttempts;
        private long successfulLogins;
        private long failedLogins;
        private long suspiciousActivities;
        private long securityAlerts;
        private List<AuditLogResponse> recentActivities;
        private List<LoginLogResponse> loginLogs;
        private List<SuspiciousActivityResponse> suspiciousActivityLogs;
        private List<SecurityAlertResponse> securityAlertLogs;

        public SecurityAnalyticsResponseBuilder totalLoginAttempts(long totalLoginAttempts) { this.totalLoginAttempts = totalLoginAttempts; return this; }
        public SecurityAnalyticsResponseBuilder successfulLogins(long successfulLogins) { this.successfulLogins = successfulLogins; return this; }
        public SecurityAnalyticsResponseBuilder failedLogins(long failedLogins) { this.failedLogins = failedLogins; return this; }
        public SecurityAnalyticsResponseBuilder suspiciousActivities(long suspiciousActivities) { this.suspiciousActivities = suspiciousActivities; return this; }
        public SecurityAnalyticsResponseBuilder securityAlerts(long securityAlerts) { this.securityAlerts = securityAlerts; return this; }
        public SecurityAnalyticsResponseBuilder recentActivities(List<AuditLogResponse> recentActivities) { this.recentActivities = recentActivities; return this; }
        public SecurityAnalyticsResponseBuilder loginLogs(List<LoginLogResponse> loginLogs) { this.loginLogs = loginLogs; return this; }
        public SecurityAnalyticsResponseBuilder suspiciousActivityLogs(List<SuspiciousActivityResponse> suspiciousActivityLogs) { this.suspiciousActivityLogs = suspiciousActivityLogs; return this; }
        public SecurityAnalyticsResponseBuilder securityAlertLogs(List<SecurityAlertResponse> securityAlertLogs) { this.securityAlertLogs = securityAlertLogs; return this; }

        public SecurityAnalyticsResponse build() {
            return new SecurityAnalyticsResponse(totalLoginAttempts, successfulLogins, failedLogins,
                    suspiciousActivities, securityAlerts, recentActivities, loginLogs, suspiciousActivityLogs, securityAlertLogs);
        }
    }
}
