package com.securevault.service;

public interface EmailService {
    void sendVerificationEmail(String recipientEmail, String fullName, String verificationToken);
    void sendOtpEmail(String recipientEmail, String fullName, String otpCode);
    void sendPasswordResetConfirmationEmail(String recipientEmail, String fullName);
}
