package com.securevault.dto.response;

public class PasswordHealthReportResponse {

    private long totalCredentials;
    private long strongPasswords;
    private long mediumPasswords;
    private long weakPasswords;
    private int healthScore;

    public PasswordHealthReportResponse() {}

    public PasswordHealthReportResponse(long totalCredentials, long strongPasswords, long mediumPasswords, long weakPasswords, int healthScore) {
        this.totalCredentials = totalCredentials;
        this.strongPasswords = strongPasswords;
        this.mediumPasswords = mediumPasswords;
        this.weakPasswords = weakPasswords;
        this.healthScore = healthScore;
    }

    public long getTotalCredentials() { return totalCredentials; }
    public void setTotalCredentials(long totalCredentials) { this.totalCredentials = totalCredentials; }

    public long getStrongPasswords() { return strongPasswords; }
    public void setStrongPasswords(long strongPasswords) { this.strongPasswords = strongPasswords; }

    public long getMediumPasswords() { return mediumPasswords; }
    public void setMediumPasswords(long mediumPasswords) { this.mediumPasswords = mediumPasswords; }

    public long getWeakPasswords() { return weakPasswords; }
    public void setWeakPasswords(long weakPasswords) { this.weakPasswords = weakPasswords; }

    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }

    public static PasswordHealthReportResponseBuilder builder() { return new PasswordHealthReportResponseBuilder(); }

    public static class PasswordHealthReportResponseBuilder {
        private long totalCredentials;
        private long strongPasswords;
        private long mediumPasswords;
        private long weakPasswords;
        private int healthScore;

        public PasswordHealthReportResponseBuilder totalCredentials(long totalCredentials) { this.totalCredentials = totalCredentials; return this; }
        public PasswordHealthReportResponseBuilder strongPasswords(long strongPasswords) { this.strongPasswords = strongPasswords; return this; }
        public PasswordHealthReportResponseBuilder mediumPasswords(long mediumPasswords) { this.mediumPasswords = mediumPasswords; return this; }
        public PasswordHealthReportResponseBuilder weakPasswords(long weakPasswords) { this.weakPasswords = weakPasswords; return this; }
        public PasswordHealthReportResponseBuilder healthScore(int healthScore) { this.healthScore = healthScore; return this; }

        public PasswordHealthReportResponse build() {
            return new PasswordHealthReportResponse(totalCredentials, strongPasswords, mediumPasswords, weakPasswords, healthScore);
        }
    }
}
