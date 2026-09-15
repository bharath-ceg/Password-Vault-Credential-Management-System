package com.securevault.service.impl;

import com.securevault.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply.securevault@gmail.com}")
    private String fromEmail;

    @Value("${app.email.verification-base-url:http://localhost:5173/verify-email}")
    private String verificationBaseUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendVerificationEmail(String recipientEmail, String fullName, String verificationToken) {
        String verificationUrl = verificationBaseUrl + "?token=" + verificationToken;

        log.info("================================================================================");
        log.info("📧 [LOCAL DEV VERIFICATION LINK FOR {}]:", recipientEmail);
        log.info("👉 {}", verificationUrl);
        log.info("================================================================================");

        try {
            String template = loadTemplate("templates/verification-email.html");
            String content = template.replace("{{NAME}}", fullName).replace("{{LINK}}", verificationUrl);

            sendHtmlEmail(recipientEmail, "SecureVault – Verify Your Email Address", content);
            log.info("Verification email dispatched via SMTP to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused (Configure real SMTP_PASSWORD for inbox delivery): {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendOtpEmail(String recipientEmail, String fullName, String otpCode) {
        log.info("================================================================================");
        log.info("🔑 [LOCAL DEV PASSWORD RESET OTP FOR {}]:", recipientEmail);
        log.info("👉 OTP CODE: {}", otpCode);
        log.info("================================================================================");

        try {
            String template = loadTemplate("templates/otp-email.html");
            String content = template.replace("{{NAME}}", fullName).replace("{{OTP}}", otpCode);

            sendHtmlEmail(recipientEmail, "SecureVault – Password Reset OTP", content);
            log.info("OTP email dispatched via SMTP to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused (Configure real SMTP_PASSWORD for inbox delivery): {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPasswordResetConfirmationEmail(String recipientEmail, String fullName) {
        try {
            String template = loadTemplate("templates/password-reset-confirmation.html");
            String content = template.replace("{{NAME}}", fullName);

            sendHtmlEmail(recipientEmail, "SecureVault – Password Reset Successful", content);
            log.info("Password reset confirmation email dispatched via SMTP to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendSuccessfulLoginNotificationEmail(String recipientEmail, String fullName, ZonedDateTime loginTime) {
        try {
            String formattedTime = loginTime != null ? loginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")) : ZonedDateTime.now().toString();
            String template = loadTemplate("templates/successful-login-notification.html");
            String content = template.replace("{{NAME}}", fullName != null ? fullName : recipientEmail).replace("{{DATE_TIME}}", formattedTime);

            sendHtmlEmail(recipientEmail, "New login detected on your SecureVault account", content);
            log.info("Successful login notification email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendSecurityAlertNotificationEmail(String recipientEmail, String fullName) {
        try {
            String template = loadTemplate("templates/failed-login-notification.html");
            String content = template.replace("{{NAME}}", fullName != null ? fullName : recipientEmail);

            sendHtmlEmail(recipientEmail, "Security alert – Multiple failed login attempts", content);
            log.info("Multiple failed login security email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendCredentialSharedNotificationEmail(String recipientEmail, String recipientName, String senderName, String credentialAlias) {
        try {
            String template = loadTemplate("templates/credential-shared-notification.html");
            String content = template
                    .replace("{{NAME}}", recipientName != null ? recipientName : recipientEmail)
                    .replace("{{SENDER_NAME}}", senderName != null ? senderName : "A SecureVault user")
                    .replace("{{CREDENTIAL_ALIAS}}", credentialAlias != null ? credentialAlias : "Vault Item");

            sendHtmlEmail(recipientEmail, "A credential has been securely shared with you", content);
            log.info("Credential shared notification email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPasswordHealthNotificationEmail(String recipientEmail, String fullName, String accountName) {
        try {
            String template = loadTemplate("templates/password-health-notification.html");
            String content = template
                    .replace("{{NAME}}", fullName != null ? fullName : recipientEmail)
                    .replace("{{ACCOUNT_NAME}}", accountName != null ? accountName : "Account");

            sendHtmlEmail(recipientEmail, "Password health alert – Action required", content);
            log.info("Password health notification email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPasswordExpirationNotificationEmail(String recipientEmail, String fullName, String accountName) {
        try {
            String template = loadTemplate("templates/password-expiration-notification.html");
            String content = template
                    .replace("{{NAME}}", fullName != null ? fullName : recipientEmail)
                    .replace("{{ACCOUNT_NAME}}", accountName != null ? accountName : "Account");

            sendHtmlEmail(recipientEmail, "Password expiration reminder", content);
            log.info("Password expiration reminder email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendSharedPasswordExpirationNotificationEmail(String recipientEmail, String recipientName, String senderName, String accountName) {
        try {
            String template = loadTemplate("templates/shared-password-expiration-notification.html");
            String content = template
                    .replace("{{NAME}}", recipientName != null ? recipientName : recipientEmail)
                    .replace("{{SENDER_NAME}}", senderName != null ? senderName : "Credential Owner")
                    .replace("{{ACCOUNT_NAME}}", accountName != null ? accountName : "Shared Account");

            sendHtmlEmail(recipientEmail, "Shared password update reminder", content);
            log.info("Shared password expiration reminder email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendSuspiciousActivityNotificationEmail(String recipientEmail, String fullName, String activityDetails) {
        try {
            String template = loadTemplate("templates/suspicious-activity-notification.html");
            String content = template
                    .replace("{{NAME}}", fullName != null ? fullName : recipientEmail)
                    .replace("{{ACTIVITY_DETAILS}}", activityDetails != null ? activityDetails : "Suspicious activity was detected on your SecureVault account.");

            sendHtmlEmail(recipientEmail, "Suspicious activity detected on your SecureVault account", content);
            log.info("Suspicious activity notification email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP email dispatch to {} paused: {}", recipientEmail, e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    private String loadTemplate(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
