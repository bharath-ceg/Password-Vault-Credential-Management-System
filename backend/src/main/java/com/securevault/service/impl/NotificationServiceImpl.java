package com.securevault.service.impl;

import com.securevault.dto.response.NotificationResponse;
import com.securevault.dto.response.PasswordGenerationResponse;
import com.securevault.dto.response.UnreadNotificationCountResponse;
import com.securevault.entity.CredentialShare;
import com.securevault.entity.Notification;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.entity.enums.NotificationType;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.CredentialShareRepository;
import com.securevault.repository.NotificationRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.service.EmailService;
import com.securevault.service.NotificationService;
import com.securevault.service.PasswordGeneratorService;
import com.securevault.util.AESEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final VaultCredentialRepository vaultCredentialRepository;
    private final CredentialShareRepository credentialShareRepository;
    private final EmailService emailService;
    private final AESEncryptionUtil aesUtil;
    private final PasswordGeneratorService passwordGeneratorService;

    @Value("${app.security.password-expiration-days:30}")
    private int passwordExpirationDays;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   VaultCredentialRepository vaultCredentialRepository,
                                   CredentialShareRepository credentialShareRepository,
                                   EmailService emailService,
                                   AESEncryptionUtil aesUtil,
                                   PasswordGeneratorService passwordGeneratorService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.vaultCredentialRepository = vaultCredentialRepository;
        this.credentialShareRepository = credentialShareRepository;
        this.emailService = emailService;
        this.aesUtil = aesUtil;
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(User user, NotificationType type, String title, String message, String referenceId) {
        if (user == null) {
            log.warn("Cannot create notification: User is null");
            return null;
        }

        User targetUser = null;
        if (user.getId() != null) {
            targetUser = userRepository.findById(user.getId()).orElse(user);
        } else if (user.getEmail() != null) {
            targetUser = userRepository.findByEmail(user.getEmail()).orElse(user);
        } else {
            targetUser = user;
        }

        // Idempotency check if referenceId is provided
        if (referenceId != null && !referenceId.trim().isEmpty()) {
            boolean exists = notificationRepository.existsByUserEmailAndTypeAndReferenceId(targetUser.getEmail(), type, referenceId.trim());
            if (exists) {
                log.info("Duplicate notification ignored for user {} with referenceId: {}", targetUser.getEmail(), referenceId);
                return null;
            }
        }

        Notification notification = Notification.builder()
                .user(targetUser)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .referenceId(referenceId != null ? referenceId.trim() : null)
                .createdAt(ZonedDateTime.now())
                .build();

        Notification saved = notificationRepository.saveAndFlush(notification);
        log.info("Created notification ID {} ({}) for user {}", saved.getId(), type, targetUser.getEmail());

        // Dispatch Email Notification Asynchronously based on event type
        dispatchEmailNotification(targetUser, type, title, message, saved.getCreatedAt());

        return mapToResponse(saved);
    }

    private void dispatchEmailNotification(User user, NotificationType type, String title, String message, ZonedDateTime createdAt) {
        try {
            switch (type) {
                case SUCCESSFUL_LOGIN ->
                        emailService.sendSuccessfulLoginNotificationEmail(user.getEmail(), user.getFullName(), createdAt);
                case FAILED_LOGIN_SECURITY ->
                        emailService.sendSecurityAlertNotificationEmail(user.getEmail(), user.getFullName());
                case CREDENTIAL_SHARED -> {
                    String credentialAlias = "Shared Credential";
                    String senderName = "A SecureVault user";

                    if (message != null && message.contains("('") && message.contains("')")) {
                        int start = message.indexOf("('") + 2;
                        int end = message.indexOf("')");
                        if (start < end) {
                            credentialAlias = message.substring(start, end);
                        }
                    }
                    if (message != null && message.contains("by ")) {
                        senderName = message.substring(message.indexOf("by ") + 3).replace(".", "").trim();
                    }
                    emailService.sendCredentialSharedNotificationEmail(user.getEmail(), user.getFullName(), senderName, credentialAlias);
                }
                case PASSWORD_EXPIRATION -> {
                    String accountName = extractAccountName(message);
                    if (message != null && message.contains("shared with you by")) {
                        String senderName = extractSenderName(message);
                        emailService.sendSharedPasswordExpirationNotificationEmail(user.getEmail(), user.getFullName(), senderName, accountName);
                    } else {
                        emailService.sendPasswordExpirationNotificationEmail(user.getEmail(), user.getFullName(), accountName);
                    }
                }
                case PASSWORD_HEALTH -> {
                    String accountName = extractAccountName(message);
                    emailService.sendPasswordHealthNotificationEmail(user.getEmail(), user.getFullName(), accountName);
                }
                case SUSPICIOUS_ACTIVITY ->
                        emailService.sendSuspiciousActivityNotificationEmail(user.getEmail(), user.getFullName(), message);
            }
        } catch (Exception e) {
            log.warn("Failed to dispatch email for notification type {}: {}", type, e.getMessage());
        }
    }

    private String extractAccountName(String message) {
        if (message == null) return "Account";
        if (message.contains("for ") && message.contains(" is ")) {
            int start = message.indexOf("for ") + 4;
            int end = message.indexOf(" is ");
            if (start < end) {
                return message.substring(start, end).trim();
            }
        }
        if (message.contains("for ") && message.contains(" shared")) {
            int start = message.indexOf("for ") + 4;
            int end = message.indexOf(" shared");
            if (start < end) {
                return message.substring(start, end).trim();
            }
        }
        return "Account";
    }

    private String extractSenderName(String message) {
        if (message == null) return "a user";
        if (message.contains("shared with you by ") && message.contains(" is old")) {
            int start = message.indexOf("shared with you by ") + 19;
            int end = message.indexOf(" is old");
            if (start < end) {
                return message.substring(start, end).trim();
            }
        }
        return "a user";
    }

    @Override
    @Transactional
    public List<NotificationResponse> getUserNotifications(String userEmail) {
        String email = userEmail != null ? userEmail.toLowerCase().trim() : "";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Evaluate password expiration and health conditions idempotently
        checkPasswordExpirationNotificationsForUser(user);

        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadNotificationCount(String userEmail) {
        String email = userEmail != null ? userEmail.toLowerCase().trim() : "";
        long unreadCount = notificationRepository.countByUserEmailAndIsReadFalse(email);
        return UnreadNotificationCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markNotificationAsRead(String userEmail, Long notificationId) {
        String email = userEmail != null ? userEmail.toLowerCase().trim() : "";

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedAccessException("You are not authorized to modify this notification.");
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void markAllNotificationsAsRead(String userEmail) {
        String email = userEmail != null ? userEmail.toLowerCase().trim() : "";
        notificationRepository.markAllAsReadByUserEmail(email);
    }

    @Override
    @Transactional
    public void checkPasswordExpirationNotifications(String userEmail) {
        String email = userEmail != null ? userEmail.toLowerCase().trim() : "";
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            checkPasswordExpirationNotificationsForUser(user);
        }
    }

    private void checkPasswordExpirationNotificationsForUser(User user) {
        if (user == null) return;
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(passwordExpirationDays);

        // 1. Check user's own credentials
        List<VaultCredential> credentials = vaultCredentialRepository.findByUserOrderByCreatedAtDesc(user);
        for (VaultCredential cred : credentials) {
            String alias = cred.getAliasName() != null && !cred.getAliasName().trim().isEmpty()
                    ? cred.getAliasName()
                    : (cred.getApplicationUrl() != null ? cred.getApplicationUrl() : "Credential #" + cred.getId());

            ZonedDateTime lastUpdated = cred.getUpdatedAt() != null ? cred.getUpdatedAt() : cred.getCreatedAt();
            String tsSuffix = lastUpdated != null ? "_" + lastUpdated.toEpochSecond() : "";

            // A. Check for Weak Password
            boolean isWeak = false;
            try {
                if (cred.getEncryptedPassword() != null) {
                    String decrypted = aesUtil.decrypt(cred.getEncryptedPassword());
                    PasswordGenerationResponse analysis = passwordGeneratorService.analyzePassword(decrypted);
                    if (analysis != null && analysis.getStrengthScore() < 40) {
                        isWeak = true;
                    }
                }
            } catch (Exception e) {
                log.debug("Could not check password strength for credential {}: {}", cred.getId(), e.getMessage());
            }

            if (isWeak) {
                String refId = "WEAK_CRED_" + cred.getId() + tsSuffix;
                String title = "Weak password detected";
                String msg = "Your password for " + alias + " is weak and needs to be changed. Please update it to maintain better security.";
                createNotification(user, NotificationType.PASSWORD_HEALTH, title, msg, refId);
            }

            // B. Check for Password Age (> 30 days)
            if (lastUpdated != null && lastUpdated.isBefore(cutoffDate)) {
                String refId = "EXP_CRED_" + cred.getId() + tsSuffix;
                String title = "Password needs to be changed";
                String msg = "Your password for " + alias + " is more than " + passwordExpirationDays + " days old. Please change it to a new password to maintain account security.";
                createNotification(user, NotificationType.PASSWORD_EXPIRATION, title, msg, refId);
            }
        }

        // 2. Check credentials shared WITH this user (User B)
        try {
            List<CredentialShare> receivedShares = credentialShareRepository.findByRecipientOrderByCreatedAtDesc(user);
            for (CredentialShare share : receivedShares) {
                if (share.isExpired()) continue;
                VaultCredential cred = share.getCredential();
                if (cred == null) continue;

                ZonedDateTime lastUpdated = cred.getUpdatedAt() != null ? cred.getUpdatedAt() : cred.getCreatedAt();
                if (lastUpdated != null && lastUpdated.isBefore(cutoffDate)) {
                    String alias = cred.getAliasName() != null && !cred.getAliasName().trim().isEmpty()
                            ? cred.getAliasName()
                            : (cred.getApplicationUrl() != null ? cred.getApplicationUrl() : "Credential #" + cred.getId());
                    String ownerName = share.getOwner() != null ? share.getOwner().getFullName() : "a user";
                    if (ownerName == null || ownerName.trim().isEmpty()) {
                        ownerName = share.getOwner() != null ? share.getOwner().getEmail() : "a user";
                    }

                    String tsSuffix = lastUpdated != null ? "_" + lastUpdated.toEpochSecond() : "";
                    String refId = "EXP_SHARE_REC_" + share.getId() + tsSuffix;
                    String title = "Shared password needs attention";
                    String msg = "The password for " + alias + " shared with you by " + ownerName + " is old and needs to be changed. Please update the password to maintain security.";
                    createNotification(user, NotificationType.PASSWORD_EXPIRATION, title, msg, refId);
                }
            }
        } catch (Exception e) {
            log.warn("Error checking shared password expirations for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .userEmail(notification.getUser() != null ? notification.getUser().getEmail() : null)
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

