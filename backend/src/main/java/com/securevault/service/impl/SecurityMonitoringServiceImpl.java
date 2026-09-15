package com.securevault.service.impl;

import com.securevault.dto.response.AuditLogResponse;
import com.securevault.dto.response.LoginLogResponse;
import com.securevault.dto.response.SecurityAlertResponse;
import com.securevault.dto.response.SecurityAnalyticsResponse;
import com.securevault.dto.response.SuspiciousActivityResponse;
import com.securevault.dto.response.UnreadSecurityCountsResponse;
import com.securevault.entity.AuditLog;
import com.securevault.entity.LoginLog;
import com.securevault.entity.SecurityAlert;
import com.securevault.entity.SuspiciousActivity;
import com.securevault.entity.User;
import com.securevault.entity.enums.NotificationType;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.AuditLogRepository;
import com.securevault.repository.LoginLogRepository;
import com.securevault.repository.SecurityAlertRepository;
import com.securevault.repository.SuspiciousActivityRepository;
import com.securevault.repository.UserRepository;
import com.securevault.service.NotificationService;
import com.securevault.service.SecurityMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityMonitoringServiceImpl implements SecurityMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(SecurityMonitoringServiceImpl.class);

    private final LoginLogRepository loginLogRepository;
    private final SuspiciousActivityRepository suspiciousActivityRepository;
    private final SecurityAlertRepository securityAlertRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.security.failed-login-threshold:3}")
    private int failedLoginThreshold;

    @Value("${app.security.failed-login-time-window-minutes:15}")
    private int failedLoginTimeWindowMinutes;

    public SecurityMonitoringServiceImpl(LoginLogRepository loginLogRepository,
                                         SuspiciousActivityRepository suspiciousActivityRepository,
                                         SecurityAlertRepository securityAlertRepository,
                                         AuditLogRepository auditLogRepository,
                                         UserRepository userRepository,
                                         NotificationService notificationService) {
        this.loginLogRepository = loginLogRepository;
        this.suspiciousActivityRepository = suspiciousActivityRepository;
        this.securityAlertRepository = securityAlertRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void recordLoginAttempt(User user, String emailStr, boolean success) {
        String email = emailStr != null ? emailStr.toLowerCase().trim() : "";
        String userName = user != null ? user.getFullName() : email;

        LoginLog loginLog = LoginLog.builder()
                .user(user)
                .userName(userName)
                .userEmail(email)
                .loginStatus(success ? "SUCCESS" : "FAILED")
                .isRead(false)
                .createdAt(ZonedDateTime.now())
                .build();

        loginLogRepository.save(loginLog);

        if (success) {
            recordAuditLog(user, email, "LOGIN_SUCCESS", "User logged in successfully.");
        } else {
            recordAuditLog(user, email, "LOGIN_FAILED", "Failed login attempt for email: " + email);
            analyzeFailedLogins(user, email);
        }
    }

    private void analyzeFailedLogins(User user, String email) {
        ZonedDateTime latestSuccess = loginLogRepository.findLatestSuccessTimeByUserEmail(email);
        ZonedDateTime latestSuspicious = suspiciousActivityRepository.findLatestDetectedTimeByUserEmail(email);

        ZonedDateTime sinceCutoff = null;
        if (latestSuccess != null && latestSuspicious != null) {
            sinceCutoff = latestSuccess.isAfter(latestSuspicious) ? latestSuccess : latestSuspicious;
        } else if (latestSuccess != null) {
            sinceCutoff = latestSuccess;
        } else if (latestSuspicious != null) {
            sinceCutoff = latestSuspicious;
        }

        long failedCount = (sinceCutoff != null) 
                ? loginLogRepository.countFailedLoginsSince(email, sinceCutoff) 
                : loginLogRepository.countAllFailedLoginsByUserEmail(email);

        log.info("Failed login count for {} since {}: {}", email, sinceCutoff, failedCount);

        if (failedCount >= failedLoginThreshold) {
            log.warn("Threshold reached ({} failed attempts). Triggering suspicious activity & security alert for {}", failedCount, email);

            SuspiciousActivity suspiciousActivity = SuspiciousActivity.builder()
                    .user(user)
                    .userEmail(email)
                    .activityType("MULTIPLE_FAILED_LOGINS")
                    .description("Multiple failed login attempts detected.")
                    .status("FLAGGED")
                    .isRead(false)
                    .detectedAt(ZonedDateTime.now())
                    .build();

            suspiciousActivityRepository.save(suspiciousActivity);

            SecurityAlert securityAlert = SecurityAlert.builder()
                    .user(user)
                    .userEmail(email)
                    .alertType("MULTIPLE_FAILED_LOGIN_ATTEMPTS")
                    .message("Multiple failed login attempts detected.")
                    .severity("HIGH")
                    .status("UNREAD")
                    .isRead(false)
                    .createdAt(ZonedDateTime.now())
                    .build();

            SecurityAlert savedAlert = securityAlertRepository.save(securityAlert);

            recordAuditLog(user, email, "SUSPICIOUS_LOGIN_ACTIVITY", "Suspicious activity detected: Multiple failed login attempts.");
            recordAuditLog(user, email, "SECURITY_ALERT_CREATED", "Security alert created: High severity alert for multiple failed logins.");

            User targetUser = user;
            if (targetUser == null && email != null && !email.isEmpty()) {
                targetUser = userRepository.findByEmail(email).orElse(null);
            }

            if (targetUser != null) {
                String alertIdStr = (savedAlert != null && savedAlert.getId() != null) ? String.valueOf(savedAlert.getId()) : String.valueOf(System.currentTimeMillis());
                String failedLoginRefId = "FAILED_LOGIN_" + alertIdStr;
                notificationService.createNotification(
                        targetUser,
                        NotificationType.FAILED_LOGIN_SECURITY,
                        "Security alert – Multiple failed login attempts",
                        "Multiple failed login attempts detected on your SecureVault account.",
                        failedLoginRefId
                );

                String activityIdStr = (suspiciousActivity != null && suspiciousActivity.getId() != null) ? String.valueOf(suspiciousActivity.getId()) : String.valueOf(System.currentTimeMillis());
                String refId = "SUSPICIOUS_" + activityIdStr;
                notificationService.createNotification(
                        targetUser,
                        NotificationType.SUSPICIOUS_ACTIVITY,
                        "Suspicious activity detected",
                        "Suspicious activity was detected on your SecureVault account.",
                        refId
                );
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuditLog(User user, String emailStr, String action, String description) {
        String email = emailStr != null ? emailStr.toLowerCase().trim() : "";
        User dbUser = null;
        if (user != null && user.getId() != null) {
            dbUser = userRepository.findById(user.getId()).orElse(null);
        }
        if (dbUser == null && !email.isEmpty()) {
            dbUser = userRepository.findByEmail(email).orElse(null);
        }

        AuditLog auditLog = AuditLog.builder()
                .user(dbUser)
                .userEmail(email)
                .action(action)
                .description(description)
                .isRead(false)
                .timestamp(ZonedDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginLogResponse> getUserLoginLogs(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        return loginLogRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(this::mapToLoginLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuspiciousActivityResponse> getUserSuspiciousActivities(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        return suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(email).stream()
                .map(this::mapToSuspiciousActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecurityAlertResponse> getUserSecurityAlerts(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        ZonedDateTime cutoff8Hours = ZonedDateTime.now().minusHours(8);

        return securityAlertRepository.findActiveAlertsByUserEmail(email, cutoff8Hours).stream()
                .map(this::mapToSecurityAlertResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SecurityAlertResponse markAlertAsRead(String userEmail, Long alertId) {
        String email = userEmail.toLowerCase().trim();
        SecurityAlert alert = securityAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Security alert not found with ID: " + alertId));

        if (!alert.getUserEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedAccessException("You are not authorized to modify this security alert.");
        }

        alert.setStatus("READ");
        alert.setIsRead(true);
        alert.setReadAt(ZonedDateTime.now());
        SecurityAlert saved = securityAlertRepository.save(alert);
        return mapToSecurityAlertResponse(saved);
    }

    @Override
    @Transactional
    public void markAllAlertsAsRead(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        securityAlertRepository.markAllAsReadByUserEmail(email);
    }

    @Override
    @Transactional
    public SuspiciousActivityResponse markSuspiciousActivityAsRead(String userEmail, Long suspiciousId) {
        String email = userEmail.toLowerCase().trim();
        SuspiciousActivity activity = suspiciousActivityRepository.findById(suspiciousId)
                .orElseThrow(() -> new ResourceNotFoundException("Suspicious activity not found with ID: " + suspiciousId));

        if (!activity.getUserEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedAccessException("You are not authorized to modify this suspicious activity.");
        }

        activity.setIsRead(true);
        activity.setStatus("FLAGGED");
        SuspiciousActivity saved = suspiciousActivityRepository.save(activity);
        return mapToSuspiciousActivityResponse(saved);
    }

    @Override
    @Transactional
    public void markAllSuspiciousActivitiesAsRead(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        suspiciousActivityRepository.markAllAsReadByUserEmail(email);
    }

    @Override
    @Transactional
    public void markLoginActivityAsRead(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        loginLogRepository.markAllAsReadByUserEmail(email);
    }

    @Override
    @Transactional
    public void markAuditLogsAsRead(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        auditLogRepository.markAllAsReadByUserEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadSecurityCountsResponse getUnreadSecurityCounts(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        ZonedDateTime cutoff8Hours = ZonedDateTime.now().minusHours(8);
        long loginUnread = loginLogRepository.countUnreadByUserEmail(email);
        long suspiciousUnread = suspiciousActivityRepository.countUnreadByUserEmail(email);
        long alertsUnread = securityAlertRepository.countActiveUnreadByUserEmail(email, cutoff8Hours);
        long auditUnread = auditLogRepository.countUnreadByUserEmail(email);

        return UnreadSecurityCountsResponse.builder()
                .loginActivityUnread(loginUnread)
                .suspiciousActivityUnread(suspiciousUnread)
                .securityAlertsUnread(alertsUnread)
                .auditLogsUnread(auditUnread)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getUserAuditLogs(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        return auditLogRepository.findByUserEmailOrderByTimestampDesc(email).stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityAnalyticsResponse getSecurityAnalytics(String userEmail) {
        String email = userEmail.toLowerCase().trim();

        List<LoginLog> allLoginLogs = loginLogRepository.findByUserEmailOrderByCreatedAtDesc(email);
        long successfulLogins = allLoginLogs.stream().filter(l -> "SUCCESS".equalsIgnoreCase(l.getLoginStatus())).count();
        long failedLogins = allLoginLogs.stream().filter(l -> "FAILED".equalsIgnoreCase(l.getLoginStatus())).count();
        long totalLoginAttempts = allLoginLogs.size();

        List<SuspiciousActivity> suspiciousList = suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(email);
        long suspiciousActivities = suspiciousList.size();

        List<SecurityAlert> alertsList = securityAlertRepository.findByUserEmailOrderByCreatedAtDesc(email);
        long securityAlerts = alertsList.size();

        List<AuditLog> auditList = auditLogRepository.findByUserEmailOrderByTimestampDesc(email);

        List<AuditLogResponse> recentActivities = auditList.stream()
                .limit(10)
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());

        List<LoginLogResponse> recentLoginLogs = allLoginLogs.stream()
                .limit(10)
                .map(this::mapToLoginLogResponse)
                .collect(Collectors.toList());

        List<SuspiciousActivityResponse> recentSuspicious = suspiciousList.stream()
                .limit(10)
                .map(this::mapToSuspiciousActivityResponse)
                .collect(Collectors.toList());

        List<SecurityAlertResponse> recentAlerts = alertsList.stream()
                .limit(10)
                .map(this::mapToSecurityAlertResponse)
                .collect(Collectors.toList());

        return SecurityAnalyticsResponse.builder()
                .totalLoginAttempts(totalLoginAttempts)
                .successfulLogins(successfulLogins)
                .failedLogins(failedLogins)
                .suspiciousActivities(suspiciousActivities)
                .securityAlerts(securityAlerts)
                .recentActivities(recentActivities)
                .loginLogs(recentLoginLogs)
                .suspiciousActivityLogs(recentSuspicious)
                .securityAlertLogs(recentAlerts)
                .build();
    }

    private LoginLogResponse mapToLoginLogResponse(LoginLog log) {
        ZonedDateTime time = log.getCreatedAt();
        return LoginLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUserName())
                .userEmail(log.getUserEmail())
                .loginDate(time != null ? time.toLocalDate() : null)
                .loginTime(time != null ? time.toLocalTime() : null)
                .loginStatus(log.getLoginStatus())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private SuspiciousActivityResponse mapToSuspiciousActivityResponse(SuspiciousActivity activity) {
        return SuspiciousActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUser() != null ? activity.getUser().getId() : null)
                .userEmail(activity.getUserEmail())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .status("FLAGGED")
                .detectedAt(activity.getDetectedAt())
                .build();
    }

    private SecurityAlertResponse mapToSecurityAlertResponse(SecurityAlert alert) {
        return SecurityAlertResponse.builder()
                .id(alert.getId())
                .userId(alert.getUser() != null ? alert.getUser().getId() : null)
                .userEmail(alert.getUserEmail())
                .alertType(alert.getAlertType())
                .message(alert.getMessage())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .readAt(alert.getReadAt())
                .build();
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userEmail(log.getUserEmail())
                .action(log.getAction())
                .description(log.getDescription())
                .timestamp(log.getTimestamp())
                .build();
    }
}
