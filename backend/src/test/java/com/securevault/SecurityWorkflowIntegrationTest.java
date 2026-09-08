package com.securevault;

import com.securevault.dto.request.*;
import com.securevault.dto.response.*;
import com.securevault.entity.User;
import com.securevault.entity.enums.CredentialCategory;
import com.securevault.entity.enums.SharePermission;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.UserRepository;
import com.securevault.service.*;
import com.securevault.util.AESEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SecurityWorkflowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private VaultService vaultService;

    @Autowired
    private CredentialShareService shareService;

    @Autowired
    private SecurityMonitoringService securityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String user1Email = "user1@securevault.com";
    private final String user2Email = "user2@securevault.com";

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail(user1Email).isEmpty()) {
            User user1 = User.builder()
                    .fullName("User One")
                    .email(user1Email)
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .isEmailVerified(true)
                    .build();
            userRepository.save(user1);
        }

        if (userRepository.findByEmail(user2Email).isEmpty()) {
            User user2 = User.builder()
                    .fullName("User Two")
                    .email(user2Email)
                    .passwordHash(passwordEncoder.encode("Password@456"))
                    .isEmailVerified(true)
                    .build();
            userRepository.save(user2);
        }
    }

    @Test
    @DisplayName("Vault Encryption & Security Test - AES-256-GCM and Password Reveal Protection")
    void testVaultEncryptionAndReveal() {
        // Create credential
        VaultCredentialRequest req = new VaultCredentialRequest("https://github.com", "GitHub Account", "user1_github", "SuperSecretKey123!");

        VaultCredentialResponse created = vaultService.createCredential(user1Email, req);
        assertNotNull(created.getId());
        assertEquals("https://github.com", created.getApplicationUrl());
        assertEquals(CredentialCategory.DEVELOPER, created.getCategory());

        // Set privacy password for user 1
        SetPrivacyPasswordRequest setPrivacyReq = new SetPrivacyPasswordRequest("PrivacyPass@123");
        vaultService.setPrivacyPassword(user1Email, setPrivacyReq);

        // Reveal password using privacy password
        RevealPasswordRequest revealReq = new RevealPasswordRequest();
        revealReq.setPrivacyPassword("PrivacyPass@123");

        String revealed = vaultService.revealPassword(user1Email, created.getId(), revealReq);
        assertEquals("SuperSecretKey123!", revealed);

        // Incorrect privacy password should fail
        RevealPasswordRequest badReveal = new RevealPasswordRequest();
        badReveal.setPrivacyPassword("WrongPassword!");
        assertThrows(UnauthorizedAccessException.class, () -> vaultService.revealPassword(user1Email, created.getId(), badReveal));
    }

    @Test
    @DisplayName("Credential Sharing & Backend Permission Enforcement (View Only vs Edit vs Full Management)")
    void testCredentialSharingPermissions() {
        // Create credential for User 1
        VaultCredentialRequest req = new VaultCredentialRequest("https://aws.amazon.com", "AWS Console", "admin", "AwsPassword999!");

        VaultCredentialResponse created = vaultService.createCredential(user1Email, req);

        // Share with User 2 as VIEW_ONLY
        CreateShareRequest shareReq = new CreateShareRequest();
        shareReq.setCredentialId(created.getId());
        shareReq.setRecipientEmail(user2Email);
        shareReq.setPermission(SharePermission.VIEW_ONLY);

        CredentialShareResponse shareResp = shareService.createShare(user1Email, shareReq);
        assertNotNull(shareResp.getId());

        // User 2 attempts edit (should fail due to VIEW_ONLY)
        VaultCredentialRequest editReq = new VaultCredentialRequest("https://aws.amazon.com", "Hacked AWS", "hacker", "HackedPassword123!");

        assertThrows(UnauthorizedAccessException.class, () -> vaultService.updateCredential(user2Email, created.getId(), editReq));

        // User 2 attempts delete (should fail)
        assertThrows(UnauthorizedAccessException.class, () -> vaultService.deleteCredential(user2Email, created.getId()));

        // Upgrade share to FULL_MANAGEMENT
        UpdateShareRequest updateShareReq = new UpdateShareRequest();
        updateShareReq.setPermission(SharePermission.FULL_MANAGEMENT);
        shareService.updateShare(user1Email, shareResp.getId(), updateShareReq);

        // Now User 2 can edit
        VaultCredentialResponse updated = vaultService.updateCredential(user2Email, created.getId(), editReq);
        assertEquals("Hacked AWS", updated.getAliasName());
    }

    @Test
    @DisplayName("Suspicious Login Threshold Test - 3 Failed Logins Trigger Alert")
    void testSuspiciousLoginThreshold() {
        String testEmail = "suspicious.user@securevault.com";

        // Attempt 1 failed login
        securityService.recordLoginAttempt(null, testEmail, false);
        assertEquals(0, securityService.getUserSuspiciousActivities(testEmail).size());

        // Attempt 2 failed login
        securityService.recordLoginAttempt(null, testEmail, false);
        assertEquals(0, securityService.getUserSuspiciousActivities(testEmail).size());

        // Attempt 3 failed login - Threshold (3) reached!
        securityService.recordLoginAttempt(null, testEmail, false);

        List<SuspiciousActivityResponse> suspiciousList = securityService.getUserSuspiciousActivities(testEmail);
        assertEquals(1, suspiciousList.size());
        assertEquals("FLAGGED", suspiciousList.get(0).getStatus());

        List<SecurityAlertResponse> alertList = securityService.getUserSecurityAlerts(testEmail);
        assertEquals(1, alertList.size());
        assertEquals("HIGH", alertList.get(0).getSeverity());
        assertEquals("UNREAD", alertList.get(0).getStatus());
    }
}
