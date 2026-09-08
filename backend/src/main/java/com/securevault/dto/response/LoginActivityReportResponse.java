package com.securevault.dto.response;

import java.util.List;

public class LoginActivityReportResponse {

    private long totalAttempts;
    private long successfulLogins;
    private long failedLogins;
    private List<LoginLogResponse> recentActivities;

    public LoginActivityReportResponse() {}

    public LoginActivityReportResponse(long totalAttempts, long successfulLogins, long failedLogins, List<LoginLogResponse> recentActivities) {
        this.totalAttempts = totalAttempts;
        this.successfulLogins = successfulLogins;
        this.failedLogins = failedLogins;
        this.recentActivities = recentActivities;
    }

    public long getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; }

    public long getSuccessfulLogins() { return successfulLogins; }
    public void setSuccessfulLogins(long successfulLogins) { this.successfulLogins = successfulLogins; }

    public long getFailedLogins() { return failedLogins; }
    public void setFailedLogins(long failedLogins) { this.failedLogins = failedLogins; }

    public List<LoginLogResponse> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<LoginLogResponse> recentActivities) { this.recentActivities = recentActivities; }

    public static LoginActivityReportResponseBuilder builder() { return new LoginActivityReportResponseBuilder(); }

    public static class LoginActivityReportResponseBuilder {
        private long totalAttempts;
        private long successfulLogins;
        private long failedLogins;
        private List<LoginLogResponse> recentActivities;

        public LoginActivityReportResponseBuilder totalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; return this; }
        public LoginActivityReportResponseBuilder successfulLogins(long successfulLogins) { this.successfulLogins = successfulLogins; return this; }
        public LoginActivityReportResponseBuilder failedLogins(long failedLogins) { this.failedLogins = failedLogins; return this; }
        public LoginActivityReportResponseBuilder recentActivities(List<LoginLogResponse> recentActivities) { this.recentActivities = recentActivities; return this; }

        public LoginActivityReportResponse build() {
            return new LoginActivityReportResponse(totalAttempts, successfulLogins, failedLogins, recentActivities);
        }
    }
}
