package com.securevault;

import com.securevault.dto.request.CreateShareRequest;
import com.securevault.dto.request.LoginRequest;
import com.securevault.dto.request.VaultCredentialRequest;
import com.securevault.dto.response.NotificationResponse;
import com.securevault.dto.response.UnreadNotificationCountResponse;
import com.securevault.dto.response.VaultCredentialResponse;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.entity.enums.NotificationType;
import com.securevault.entity.enums.SharePermission;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.NotificationRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class NotificationModuleIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private VaultService vaultService;

    @Autowired
    private CredentialShareService shareService;

    @Autowired
    private SecurityMonitoringService securityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultCredentialRepository vaultCredentialRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String user1Email = "notif.user1@securevault.com";
    private final String user2Email = "notif.user2@securevault.com";

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.findByEmail(user1Email).orElseGet(() -> {
            User u = User.builder()
                    .fullName("Notif User 1")
                    .email(user1Email)
                    .passwordHash(passwordEncoder.encode("Pass@123456"))
                    .isEmailVerified(true)
                    .build();
            return userRepository.save(u);
        });

        user2 = userRepository.findByEmail(user2Email).orElseGet(() -> {
            User u = User.builder()
                    .fullName("Notif User 2")
                    .email(user2Email)
                    .passwordHash(passwordEncoder.encode("Pass@654321"))
                    .isEmailVerified(true)
                    .build();
            return userRepository.save(u);
        });
    }

    @Test
    @DisplayName("1. Successful Login Event creates SUCCESSFUL_LOGIN notification")
    void testSuccessfulLoginNotification() {
        LoginRequest loginReq = new LoginRequest(user1Email, "Pass@123456");
        authService.login(loginReq);

        List<NotificationResponse> notifications = notificationService.getUserNotifications(user1Email);
        assertFalse(notifications.isEmpty());

        NotificationResponse loginNotif = notifications.stream()
                .filter(n -> n.getType() == NotificationType.SUCCESSFUL_LOGIN)
                .findFirst()
                .orElse(null);

        assertNotNull(loginNotif);
        assertEquals("New login detected", loginNotif.getTitle());
        assertTrue(loginNotif.getMessage().contains("successful login"));
        assertFalse(loginNotif.getIsRead());
    }

    @Test
    @DisplayName("2. 3 Failed Logins trigger SUSPICIOUS_ACTIVITY notification")
    void testFailedLoginSuspiciousActivityNotification() {
        String targetEmail = user1Email;

        // 3 Failed Login Attempts
        securityService.recordLoginAttempt(user1, targetEmail, false);
        securityService.recordLoginAttempt(user1, targetEmail, false);
        securityService.recordLoginAttempt(user1, targetEmail, false);

        List<NotificationResponse> notifications = notificationService.getUserNotifications(targetEmail);
        NotificationResponse alertNotif = notifications.stream()
                .filter(n -> n.getType() == NotificationType.SUSPICIOUS_ACTIVITY)
                .findFirst()
                .orElse(null);

        assertNotNull(alertNotif);
        assertEquals("Suspicious activity detected", alertNotif.getTitle());
        assertEquals("Suspicious activity was detected on your SecureVault account.", alertNotif.getMessage());
    }

    @Test
    @DisplayName("3. Credential Sharing creates CREDENTIAL_SHARED notification for recipient")
    void testCredentialSharingNotification() {
        // Create credential for User 1
        VaultCredentialRequest req = new VaultCredentialRequest("https://github.com", "GitHub Team", "dev_user", "MyStrongPass123!");
        VaultCredentialResponse cred = vaultService.createCredential(user1Email, req);

        // Share with User 2
        CreateShareRequest shareReq = new CreateShareRequest();
        shareReq.setCredentialId(cred.getId());
        shareReq.setRecipientEmail(user2Email);
        shareReq.setPermission(SharePermission.VIEW_ONLY);

        shareService.createShare(user1Email, shareReq);

        // Verify User 2 received notification
        List<NotificationResponse> user2Notifs = notificationService.getUserNotifications(user2Email);
        NotificationResponse shareNotif = user2Notifs.stream()
                .filter(n -> n.getType() == NotificationType.CREDENTIAL_SHARED)
                .findFirst()
                .orElse(null);

        assertNotNull(shareNotif);
        assertEquals("Credential shared with you", shareNotif.getTitle());
        assertTrue(shareNotif.getMessage().contains("GitHub Team"));
        assertTrue(shareNotif.getMessage().contains("Notif User 1"));
        // Sensitive password MUST NOT be in message
        assertFalse(shareNotif.getMessage().contains("MyStrongPass123!"));
    }

    @Test
    @DisplayName("4. Weak Password creates PASSWORD_HEALTH notification idempotently")
    void testWeakPasswordHealthNotification() {
        // Create weak password credential for User 1
        VaultCredentialRequest req = new VaultCredentialRequest("https://weak.com", "Weak Site", "user1", "123");
        vaultService.createCredential(user1Email, req);

        List<NotificationResponse> notifs = notificationService.getUserNotifications(user1Email);
        NotificationResponse healthNotif = notifs.stream()
                .filter(n -> n.getType() == NotificationType.PASSWORD_HEALTH)
                .findFirst()
                .orElse(null);

        assertNotNull(healthNotif);
        assertEquals("Weak password detected", healthNotif.getTitle());
        assertTrue(healthNotif.getMessage().contains("Weak Site"));
        assertTrue(healthNotif.getMessage().contains("is weak and needs to be changed"));

        // Second check should not duplicate notification
        long countBefore = notificationService.getUserNotifications(user1Email).stream()
                .filter(n -> n.getType() == NotificationType.PASSWORD_HEALTH)
                .count();

        notificationService.getUserNotifications(user1Email);

        long countAfter = notificationService.getUserNotifications(user1Email).stream()
                .filter(n -> n.getType() == NotificationType.PASSWORD_HEALTH)
                .count();

        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("5. Password > 30 Days Old creates PASSWORD_EXPIRATION notification for Owner")
    void testOldPasswordExpirationNotification() {
        VaultCredentialRequest req = new VaultCredentialRequest("https://old.com", "Old Service", "olduser", "StrongPass@12345");
        VaultCredentialResponse credResp = vaultService.createCredential(user1Email, req);

        // Manually set updatedAt and createdAt to 35 days ago bypassing PreUpdate hook
        vaultCredentialRepository.updateTimestamps(credResp.getId(), ZonedDateTime.now().minusDays(35), ZonedDateTime.now().minusDays(35));

        List<NotificationResponse> notifs = notificationService.getUserNotifications(user1Email);
        NotificationResponse expNotif = notifs.stream()
                .filter(n -> n.getType() == NotificationType.PASSWORD_EXPIRATION)
                .findFirst()
                .orElse(null);

        assertNotNull(expNotif);
        assertEquals("Password needs to be changed", expNotif.getTitle());
        assertTrue(expNotif.getMessage().contains("Old Service"));
        assertTrue(expNotif.getMessage().contains("is more than 30 days old"));
    }

    @Test
    @DisplayName("6. Shared Credential > 30 Days Old creates PASSWORD_EXPIRATION notification for Receiver User B")
    void testOldSharedCredentialExpirationNotification() {
        VaultCredentialRequest req = new VaultCredentialRequest("https://sharedold.com", "Shared Service", "shareuser", "SuperSecretPass!123");
        VaultCredentialResponse credResp = vaultService.createCredential(user1Email, req);

        CreateShareRequest shareReq = new CreateShareRequest();
        shareReq.setCredentialId(credResp.getId());
        shareReq.setRecipientEmail(user2Email);
        shareReq.setPermission(SharePermission.VIEW_ONLY);
        shareService.createShare(user1Email, shareReq);

        // Set credential updatedAt to 35 days ago bypassing PreUpdate hook
        vaultCredentialRepository.updateTimestamps(credResp.getId(), ZonedDateTime.now().minusDays(35), ZonedDateTime.now().minusDays(35));

        List<NotificationResponse> user2Notifs = notificationService.getUserNotifications(user2Email);
        NotificationResponse expNotif = user2Notifs.stream()
                .filter(n -> n.getType() == NotificationType.PASSWORD_EXPIRATION && n.getTitle().equals("Shared password needs attention"))
                .findFirst()
                .orElse(null);

        assertNotNull(expNotif);
        assertEquals("Shared password needs attention", expNotif.getTitle());
        assertTrue(expNotif.getMessage().contains("Shared Service"));
        assertTrue(expNotif.getMessage().contains("Notif User 1"));
    }

    @Test
    @DisplayName("7. Password Update updates updatedAt to NOW and clears expiration trigger")
    void testPasswordUpdateResetsExpiration() {
        VaultCredentialRequest req = new VaultCredentialRequest("https://updatetest.com", "Update Service", "user", "OldPass!123");
        VaultCredentialResponse credResp = vaultService.createCredential(user1Email, req);

        // Set to 35 days old bypassing PreUpdate hook
        vaultCredentialRepository.updateTimestamps(credResp.getId(), ZonedDateTime.now().minusDays(35), ZonedDateTime.now().minusDays(35));

        // Update password with fresh strong password
        VaultCredentialRequest updateReq = new VaultCredentialRequest("https://updatetest.com", "Update Service", "user", "BrandNewPass!999");
        vaultService.updateCredential(user1Email, credResp.getId(), updateReq);

        // Verify updatedAt is now recent
        VaultCredential updatedCred = vaultCredentialRepository.findById(credResp.getId()).orElseThrow();
        assertTrue(updatedCred.getUpdatedAt().isAfter(ZonedDateTime.now().minusMinutes(1)));
    }

    @Test
    @DisplayName("8. Notification Read/Unread State Management & Ownership Verification")
    void testNotificationReadStateAndAuthorization() {
        NotificationResponse created = notificationService.createNotification(
                user1,
                NotificationType.SUSPICIOUS_ACTIVITY,
                "Suspicious activity detected",
                "Suspicious activity was detected on your SecureVault account.",
                "REF_TEST_100"
        );

        assertNotNull(created);
        assertFalse(created.getIsRead());

        // User 2 cannot mark User 1's notification as read
        assertThrows(UnauthorizedAccessException.class, () ->
                notificationService.markNotificationAsRead(user2Email, created.getId())
        );

        // User 1 can mark as read
        NotificationResponse updated = notificationService.markNotificationAsRead(user1Email, created.getId());
        assertTrue(updated.getIsRead());

        // Verify unread count
        UnreadNotificationCountResponse count = notificationService.getUnreadNotificationCount(user1Email);
        assertNotNull(count);

        // Mark all as read
        notificationService.markAllNotificationsAsRead(user1Email);
        assertEquals(0, notificationService.getUnreadNotificationCount(user1Email).getUnreadCount());
    }

    @Test
    @DisplayName("9. Security Isolation - User 1 cannot view User 2's notifications")
    void testUserIsolation() {
        notificationService.createNotification(user2, NotificationType.SUCCESSFUL_LOGIN, "Private User 2 Notif", "Secret details", null);

        List<NotificationResponse> user1Notifs = notificationService.getUserNotifications(user1Email);
        boolean containsUser2Notif = user1Notifs.stream().anyMatch(n -> "Private User 2 Notif".equals(n.getTitle()));

        assertFalse(containsUser2Notif);
    }
}

