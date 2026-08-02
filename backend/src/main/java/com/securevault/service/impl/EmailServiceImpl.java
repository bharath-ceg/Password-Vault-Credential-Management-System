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

        // Print High-Visibility Verification Link in Console for Instant Dev Testing
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
        // Print High-Visibility OTP Code in Console for Instant Dev Testing
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
