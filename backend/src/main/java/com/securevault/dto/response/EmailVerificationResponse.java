package com.securevault.dto.response;

public class EmailVerificationResponse {

    private String status; // "SUCCESS", "ALREADY_VERIFIED", "EXPIRED", "INVALID"
    private String message;
    private String email;

    public EmailVerificationResponse() {}

    public EmailVerificationResponse(String status, String message, String email) {
        this.status = status;
        this.message = message;
        this.email = email;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
