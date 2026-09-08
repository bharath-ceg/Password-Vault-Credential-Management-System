package com.securevault.dto.response;

public class UnreadSecurityCountsResponse {

    private long loginActivityUnread;
    private long suspiciousActivityUnread;
    private long securityAlertsUnread;
    private long auditLogsUnread;
    private long totalSecurityUnread;

    public UnreadSecurityCountsResponse() {}

    public UnreadSecurityCountsResponse(long loginActivityUnread, long suspiciousActivityUnread, long securityAlertsUnread, long auditLogsUnread, long totalSecurityUnread) {
        this.loginActivityUnread = loginActivityUnread;
        this.suspiciousActivityUnread = suspiciousActivityUnread;
        this.securityAlertsUnread = securityAlertsUnread;
        this.auditLogsUnread = auditLogsUnread;
        this.totalSecurityUnread = totalSecurityUnread;
    }

    public long getLoginActivityUnread() { return loginActivityUnread; }
    public void setLoginActivityUnread(long loginActivityUnread) { this.loginActivityUnread = loginActivityUnread; }

    public long getSuspiciousActivityUnread() { return suspiciousActivityUnread; }
    public void setSuspiciousActivityUnread(long suspiciousActivityUnread) { this.suspiciousActivityUnread = suspiciousActivityUnread; }

    public long getSecurityAlertsUnread() { return securityAlertsUnread; }
    public void setSecurityAlertsUnread(long securityAlertsUnread) { this.securityAlertsUnread = securityAlertsUnread; }

    public long getAuditLogsUnread() { return auditLogsUnread; }
    public void setAuditLogsUnread(long auditLogsUnread) { this.auditLogsUnread = auditLogsUnread; }

    public long getTotalSecurityUnread() { return totalSecurityUnread; }
    public void setTotalSecurityUnread(long totalSecurityUnread) { this.totalSecurityUnread = totalSecurityUnread; }

    public static UnreadSecurityCountsResponseBuilder builder() { return new UnreadSecurityCountsResponseBuilder(); }

    public static class UnreadSecurityCountsResponseBuilder {
        private long loginActivityUnread;
        private long suspiciousActivityUnread;
        private long securityAlertsUnread;
        private long auditLogsUnread;

        public UnreadSecurityCountsResponseBuilder loginActivityUnread(long loginActivityUnread) { this.loginActivityUnread = loginActivityUnread; return this; }
        public UnreadSecurityCountsResponseBuilder suspiciousActivityUnread(long suspiciousActivityUnread) { this.suspiciousActivityUnread = suspiciousActivityUnread; return this; }
        public UnreadSecurityCountsResponseBuilder securityAlertsUnread(long securityAlertsUnread) { this.securityAlertsUnread = securityAlertsUnread; return this; }
        public UnreadSecurityCountsResponseBuilder auditLogsUnread(long auditLogsUnread) { this.auditLogsUnread = auditLogsUnread; return this; }

        public UnreadSecurityCountsResponse build() {
            long total = suspiciousActivityUnread + securityAlertsUnread;
            return new UnreadSecurityCountsResponse(loginActivityUnread, suspiciousActivityUnread, securityAlertsUnread, auditLogsUnread, total);
        }
    }
}
