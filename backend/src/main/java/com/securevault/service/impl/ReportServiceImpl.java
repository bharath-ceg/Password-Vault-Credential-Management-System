package com.securevault.service.impl;

import com.securevault.dto.response.LoginActivityReportResponse;
import com.securevault.dto.response.LoginLogResponse;
import com.securevault.dto.response.PasswordGenerationResponse;
import com.securevault.dto.response.PasswordHealthReportResponse;
import com.securevault.entity.LoginLog;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.repository.LoginLogRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.service.NotificationService;
import com.securevault.service.PasswordGeneratorService;
import com.securevault.service.ReportService;
import com.securevault.util.AESEncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final VaultCredentialRepository vaultRepository;
    private final LoginLogRepository loginLogRepository;
    private final AESEncryptionUtil aesUtil;
    private final PasswordGeneratorService passwordGeneratorService;
    private final NotificationService notificationService;

    public ReportServiceImpl(UserRepository userRepository,
                             VaultCredentialRepository vaultRepository,
                             LoginLogRepository loginLogRepository,
                             AESEncryptionUtil aesUtil,
                             PasswordGeneratorService passwordGeneratorService,
                             NotificationService notificationService) {
        this.userRepository = userRepository;
        this.vaultRepository = vaultRepository;
        this.loginLogRepository = loginLogRepository;
        this.aesUtil = aesUtil;
        this.passwordGeneratorService = passwordGeneratorService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PasswordHealthReportResponse getPasswordHealthReport(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        notificationService.checkPasswordExpirationNotifications(email);

        List<VaultCredential> credentials = vaultRepository.findByUserOrderByCreatedAtDesc(user);

        if (credentials.isEmpty()) {
            return PasswordHealthReportResponse.builder()
                    .totalCredentials(0)
                    .strongPasswords(0)
                    .mediumPasswords(0)
                    .weakPasswords(0)
                    .healthScore(100)
                    .build();
        }

        long strongCount = 0;
        long mediumCount = 0;
        long weakCount = 0;
        int totalScoreSum = 0;

        for (VaultCredential cred : credentials) {
            String decryptedPassword = "";
            try {
                decryptedPassword = aesUtil.decrypt(cred.getEncryptedPassword());
            } catch (Exception e) {
                // Ignore decryption failure gracefully if key mismatch occurs
            }

            PasswordGenerationResponse analysis = passwordGeneratorService.analyzePassword(decryptedPassword);
            int score = analysis.getStrengthScore();
            totalScoreSum += score;

            if (score < 40) {
                weakCount++;
            } else if (score < 70) {
                mediumCount++;
            } else {
                strongCount++;
            }
        }

        int totalCount = credentials.size();
        int healthScore = (int) Math.round((double) totalScoreSum / totalCount);

        return PasswordHealthReportResponse.builder()
                .totalCredentials(totalCount)
                .strongPasswords(strongCount)
                .mediumPasswords(mediumCount)
                .weakPasswords(weakCount)
                .healthScore(healthScore)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginActivityReportResponse getLoginActivityReport(String userEmail) {
        String email = userEmail.toLowerCase().trim();
        List<LoginLog> logs = loginLogRepository.findByUserEmailOrderByCreatedAtDesc(email);

        long totalAttempts = logs.size();
        long successfulLogins = logs.stream().filter(l -> "SUCCESS".equalsIgnoreCase(l.getLoginStatus())).count();
        long failedLogins = logs.stream().filter(l -> "FAILED".equalsIgnoreCase(l.getLoginStatus())).count();

        List<LoginLogResponse> recentActivities = logs.stream()
                .limit(10)
                .map(this::mapToLoginLogResponse)
                .collect(Collectors.toList());

        return LoginActivityReportResponse.builder()
                .totalAttempts(totalAttempts)
                .successfulLogins(successfulLogins)
                .failedLogins(failedLogins)
                .recentActivities(recentActivities)
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
}
