package com.securevault.service;

import com.securevault.dto.response.SecurityAlertResponse;
import com.securevault.dto.response.SuspiciousActivityResponse;
import com.securevault.dto.response.UnreadSecurityCountsResponse;
import com.securevault.entity.AuditLog;
import com.securevault.entity.LoginLog;
import com.securevault.entity.SecurityAlert;
import com.securevault.entity.SuspiciousActivity;
import com.securevault.entity.User;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.AuditLogRepository;
import com.securevault.repository.LoginLogRepository;
import com.securevault.repository.SecurityAlertRepository;
import com.securevault.repository.SuspiciousActivityRepository;
import com.securevault.service.impl.SecurityMonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SecurityMonitoringServiceTest {

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private SuspiciousActivityRepository suspiciousActivityRepository;

    @Mock
    private SecurityAlertRepository securityAlertRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private com.securevault.repository.UserRepository userRepository;

    @Mock
    private com.securevault.service.NotificationService notificationService;

    @InjectMocks
    private SecurityMonitoringServiceImpl securityMonitoringService;

    private User testUser;
    private final String testEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email(testEmail)
                .isEmailVerified(true)
                .build();
    }

    @Test
    @DisplayName("Record Failed Login - Triggers Suspicious Activity & Security Alert when threshold (3) reached")
    void testRecordFailedLogin_ThresholdReached() {
        org.springframework.test.util.ReflectionTestUtils.setField(securityMonitoringService, "failedLoginThreshold", 3);
        when(loginLogRepository.findLatestSuccessTimeByUserEmail(testEmail)).thenReturn(null);
        when(suspiciousActivityRepository.findLatestDetectedTimeByUserEmail(testEmail)).thenReturn(null);
        when(loginLogRepository.countFailedLoginsSince(eq(testEmail), any())).thenReturn(3L);
        when(loginLogRepository.countAllFailedLoginsByUserEmail(eq(testEmail))).thenReturn(3L);

        // Act
        securityMonitoringService.recordLoginAttempt(testUser, testEmail, false);

        verify(notificationService, times(1)).createNotification(
                eq(testUser),
                eq(com.securevault.entity.enums.NotificationType.FAILED_LOGIN_SECURITY),
                anyString(),
                anyString(),
                anyString()
        );
        verify(notificationService, times(1)).createNotification(
                eq(testUser),
                eq(com.securevault.entity.enums.NotificationType.SUSPICIOUS_ACTIVITY),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("GetUserSecurityAlerts - Filters alerts created within 8 hours")
    void testGetUserSecurityAlerts_8HourExpirationFilter() {
        // Arrange
        SecurityAlert alert = SecurityAlert.builder()
                .id(100L)
                .user(testUser)
                .userEmail(testEmail)
                .alertType("MULTIPLE_FAILED_LOGIN_ATTEMPTS")
                .message("Multiple failed login attempts detected.")
                .severity("HIGH")
                .status("UNREAD")
                .createdAt(ZonedDateTime.now().minusHours(2))
                .build();

        when(securityAlertRepository.findActiveAlertsByUserEmail(eq(testEmail), any(ZonedDateTime.class)))
                .thenReturn(List.of(alert));

        // Act
        List<SecurityAlertResponse> result = securityMonitoringService.getUserSecurityAlerts(testEmail);

        // Assert
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals("UNREAD", result.get(0).getStatus());
        assertEquals("HIGH", result.get(0).getSeverity());
    }

    @Test
    @DisplayName("MarkAlertAsRead - Updates status to READ and sets readAt timestamp")
    void testMarkAlertAsRead_Success() {
        // Arrange
        SecurityAlert alert = SecurityAlert.builder()
                .id(200L)
                .user(testUser)
                .userEmail(testEmail)
                .alertType("MULTIPLE_FAILED_LOGIN_ATTEMPTS")
                .message("Multiple failed login attempts detected.")
                .severity("HIGH")
                .status("UNREAD")
                .isRead(false)
                .createdAt(ZonedDateTime.now().minusMinutes(30))
                .build();

        when(securityAlertRepository.findById(200L)).thenReturn(Optional.of(alert));
        when(securityAlertRepository.save(any(SecurityAlert.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        SecurityAlertResponse response = securityMonitoringService.markAlertAsRead(testEmail, 200L);

        // Assert
        assertEquals("READ", response.getStatus());
        assertNotNull(response.getReadAt());
        verify(securityAlertRepository, times(1)).save(alert);
    }

    @Test
    @DisplayName("MarkAlertAsRead - Throws UnauthorizedAccessException if user email does not match")
    void testMarkAlertAsRead_Unauthorized() {
        SecurityAlert alert = SecurityAlert.builder()
                .id(201L)
                .userEmail("other@example.com")
                .build();

        when(securityAlertRepository.findById(201L)).thenReturn(Optional.of(alert));

        assertThrows(UnauthorizedAccessException.class, () -> {
            securityMonitoringService.markAlertAsRead(testEmail, 201L);
        });
    }

    @Test
    @DisplayName("GetUserSuspiciousActivities - Returns all items with status FLAGGED")
    void testGetUserSuspiciousActivities_AlwaysFlagged() {
        SuspiciousActivity activity = SuspiciousActivity.builder()
                .id(300L)
                .userEmail(testEmail)
                .activityType("MULTIPLE_FAILED_LOGINS")
                .description("Multiple failed login attempts detected.")
                .status("FLAGGED")
                .detectedAt(ZonedDateTime.now().minusHours(1))
                .build();

        when(suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(testEmail))
                .thenReturn(List.of(activity));

        List<SuspiciousActivityResponse> list = securityMonitoringService.getUserSuspiciousActivities(testEmail);

        assertEquals(1, list.size());
        assertEquals("FLAGGED", list.get(0).getStatus());
    }

    @Test
    @DisplayName("GetUnreadSecurityCounts - Returns unread count of active alerts (< 8 hours)")
    void testGetUnreadSecurityCounts() {
        when(loginLogRepository.countUnreadByUserEmail(testEmail)).thenReturn(5L);
        when(suspiciousActivityRepository.countUnreadByUserEmail(testEmail)).thenReturn(2L);
        when(securityAlertRepository.countActiveUnreadByUserEmail(eq(testEmail), any(ZonedDateTime.class))).thenReturn(1L);
        when(auditLogRepository.countUnreadByUserEmail(testEmail)).thenReturn(10L);

        UnreadSecurityCountsResponse counts = securityMonitoringService.getUnreadSecurityCounts(testEmail);

        assertEquals(5L, counts.getLoginActivityUnread());
        assertEquals(2L, counts.getSuspiciousActivityUnread());
        assertEquals(1L, counts.getSecurityAlertsUnread());
        assertEquals(10L, counts.getAuditLogsUnread());
    }

    @Test
    @DisplayName("GetSecurityAnalytics - Calculates total, successful, failed logins, suspicious, alerts, and recent activities")
    void testGetSecurityAnalytics() {
        LoginLog logSuccess = LoginLog.builder().id(1L).userEmail(testEmail).loginStatus("SUCCESS").createdAt(ZonedDateTime.now()).build();
        LoginLog logFailed = LoginLog.builder().id(2L).userEmail(testEmail).loginStatus("FAILED").createdAt(ZonedDateTime.now()).build();
        when(loginLogRepository.findByUserEmailOrderByCreatedAtDesc(testEmail)).thenReturn(List.of(logSuccess, logFailed));

        SuspiciousActivity suspicious = SuspiciousActivity.builder().id(10L).userEmail(testEmail).activityType("TEST").detectedAt(ZonedDateTime.now()).build();
        when(suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(testEmail)).thenReturn(List.of(suspicious));

        SecurityAlert alert = SecurityAlert.builder().id(20L).userEmail(testEmail).alertType("TEST").createdAt(ZonedDateTime.now()).build();
        when(securityAlertRepository.findByUserEmailOrderByCreatedAtDesc(testEmail)).thenReturn(List.of(alert));

        AuditLog audit = AuditLog.builder().id(30L).userEmail(testEmail).action("LOGIN_SUCCESS").timestamp(ZonedDateTime.now()).build();
        when(auditLogRepository.findByUserEmailOrderByTimestampDesc(testEmail)).thenReturn(List.of(audit));

        var analytics = securityMonitoringService.getSecurityAnalytics(testEmail);

        assertEquals(2L, analytics.getTotalLoginAttempts());
        assertEquals(1L, analytics.getSuccessfulLogins());
        assertEquals(1L, analytics.getFailedLogins());
        assertEquals(1L, analytics.getSuspiciousActivities());
        assertEquals(1L, analytics.getSecurityAlerts());
        assertEquals(1, analytics.getRecentActivities().size());
        assertEquals(2, analytics.getLoginLogs().size());
    }
}
