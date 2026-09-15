package com.securevault.service;

import com.securevault.dto.response.LoginActivityReportResponse;
import com.securevault.dto.response.PasswordGenerationResponse;
import com.securevault.dto.response.PasswordHealthReportResponse;
import com.securevault.entity.LoginLog;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.repository.LoginLogRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.service.impl.ReportServiceImpl;
import com.securevault.util.AESEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VaultCredentialRepository vaultRepository;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private AESEncryptionUtil aesUtil;

    @Mock
    private PasswordGeneratorService passwordGeneratorService;

    @Mock
    private com.securevault.service.NotificationService notificationService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User testUser;
    private final String testEmail = "reportuser@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .id(1L)
                .fullName("Report User")
                .email(testEmail)
                .isEmailVerified(true)
                .build();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Password Health Report - No Credentials")
    void testPasswordHealth_NoCredentials() {
        when(vaultRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(Collections.emptyList());

        PasswordHealthReportResponse report = reportService.getPasswordHealthReport(testEmail);

        assertEquals(0, report.getTotalCredentials());
        assertEquals(0, report.getStrongPasswords());
        assertEquals(0, report.getMediumPasswords());
        assertEquals(0, report.getWeakPasswords());
        assertEquals(100, report.getHealthScore());
    }

    @Test
    @DisplayName("Password Health Report - Mixed Credentials (Strong, Medium, Weak)")
    void testPasswordHealth_MixedCredentials() {
        VaultCredential cred1 = VaultCredential.builder().id(10L).encryptedPassword("enc1").build();
        VaultCredential cred2 = VaultCredential.builder().id(20L).encryptedPassword("enc2").build();
        VaultCredential cred3 = VaultCredential.builder().id(30L).encryptedPassword("enc3").build();

        when(vaultRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(cred1, cred2, cred3));

        when(aesUtil.decrypt("enc1")).thenReturn("StrongP@ssw0rd123!");
        when(aesUtil.decrypt("enc2")).thenReturn("MediumPass12");
        when(aesUtil.decrypt("enc3")).thenReturn("12345");

        when(passwordGeneratorService.analyzePassword("StrongP@ssw0rd123!"))
                .thenReturn(PasswordGenerationResponse.builder().strengthScore(95).strengthLabel("Very Strong").build());
        when(passwordGeneratorService.analyzePassword("MediumPass12"))
                .thenReturn(PasswordGenerationResponse.builder().strengthScore(55).strengthLabel("Fair").build());
        when(passwordGeneratorService.analyzePassword("12345"))
                .thenReturn(PasswordGenerationResponse.builder().strengthScore(15).strengthLabel("Weak").build());

        PasswordHealthReportResponse report = reportService.getPasswordHealthReport(testEmail);

        assertEquals(3, report.getTotalCredentials());
        assertEquals(1, report.getStrongPasswords());
        assertEquals(1, report.getMediumPasswords());
        assertEquals(1, report.getWeakPasswords());
        // average = (95 + 55 + 15) / 3 = 165 / 3 = 55
        assertEquals(55, report.getHealthScore());
    }

    @Test
    @DisplayName("Login Activity Report - Calculates successful, failed, and total attempts correctly")
    void testLoginActivityReport() {
        LoginLog log1 = LoginLog.builder().id(1L).userEmail(testEmail).loginStatus("SUCCESS").createdAt(ZonedDateTime.now()).build();
        LoginLog log2 = LoginLog.builder().id(2L).userEmail(testEmail).loginStatus("SUCCESS").createdAt(ZonedDateTime.now()).build();
        LoginLog log3 = LoginLog.builder().id(3L).userEmail(testEmail).loginStatus("FAILED").createdAt(ZonedDateTime.now()).build();

        when(loginLogRepository.findByUserEmailOrderByCreatedAtDesc(testEmail)).thenReturn(List.of(log1, log2, log3));

        LoginActivityReportResponse report = reportService.getLoginActivityReport(testEmail);

        assertEquals(3, report.getTotalAttempts());
        assertEquals(2, report.getSuccessfulLogins());
        assertEquals(1, report.getFailedLogins());
        assertEquals(3, report.getRecentActivities().size());
    }
}
