package com.securevault.service;

import java.time.ZonedDateTime;

public interface EmailService {
    void sendVerificationEmail(String recipientEmail, String fullName, String verificationToken);
    void sendOtpEmail(String recipientEmail, String fullName, String otpCode);
    void sendPasswordResetConfirmationEmail(String recipientEmail, String fullName);
    void sendSuccessfulLoginNotificationEmail(String recipientEmail, String fullName, ZonedDateTime loginTime);
    void sendSecurityAlertNotificationEmail(String recipientEmail, String fullName);
    void sendCredentialSharedNotificationEmail(String recipientEmail, String recipientName, String senderName, String credentialAlias);
    void sendPasswordHealthNotificationEmail(String recipientEmail, String fullName, String accountName);
    void sendPasswordExpirationNotificationEmail(String recipientEmail, String fullName, String accountName);
    void sendSharedPasswordExpirationNotificationEmail(String recipientEmail, String recipientName, String senderName, String accountName);
    void sendSuspiciousActivityNotificationEmail(String recipientEmail, String fullName, String activityDetails);
}
