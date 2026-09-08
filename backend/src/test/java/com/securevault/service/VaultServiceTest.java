package com.securevault.service;

import com.securevault.dto.request.RevealPasswordRequest;
import com.securevault.dto.request.VaultCredentialRequest;
import com.securevault.dto.response.VaultCredentialResponse;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.entity.enums.CredentialCategory;
import com.securevault.exception.BadRequestException;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.CredentialShareRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.service.impl.VaultServiceImpl;
import com.securevault.util.AESEncryptionUtil;
import com.securevault.util.CategoryAutoDetectorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VaultServiceTest {

    @Mock
    private VaultCredentialRepository vaultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CredentialShareRepository shareRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityMonitoringService securityMonitoringService;

    private AESEncryptionUtil aesUtil = new AESEncryptionUtil();
    private CategoryAutoDetectorUtil categoryDetector = new CategoryAutoDetectorUtil();

    private VaultServiceImpl vaultService;

    private User testUser;
    private VaultCredential testCredential;
    private final String testEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(aesUtil, "secretKeyString", "v8y/B?E(G+KbPeShVmYq3t6w9z$C&F)J");

        vaultService = new VaultServiceImpl(
                vaultRepository,
                userRepository,
                shareRepository,
                aesUtil,
                categoryDetector,
                passwordEncoder,
                securityMonitoringService
        );

        testUser = User.builder()
                .id(1L)
                .fullName("Vault User")
                .email(testEmail)
                .passwordHash("hashedPassword")
                .privacyPasswordHash("hashedPrivacy")
                .build();

        testCredential = VaultCredential.builder()
                .id(10L)
                .user(testUser)
                .aliasName("Google Account")
                .applicationUrl("https://google.com")
                .username("user@google.com")
                .encryptedPassword(aesUtil.encrypt("MySecretPass123!"))
                .category(CredentialCategory.DEVELOPER)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Create Credential - Success")
    void testCreateCredential_Success() throws Exception {
        VaultCredentialRequest request = new VaultCredentialRequest("https://google.com", "Google Account", "user@google.com", "MySecretPass123!");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(vaultRepository.save(any(VaultCredential.class))).thenReturn(testCredential);

        VaultCredentialResponse response = vaultService.createCredential(testEmail, request);

        assertNotNull(response);
        assertEquals("Google Account", response.getAliasName());
        assertEquals(CredentialCategory.DEVELOPER, response.getCategory());
        verify(vaultRepository, times(1)).save(any(VaultCredential.class));
    }

    @Test
    @DisplayName("Create Credential - Missing both URL and Alias")
    void testCreateCredential_MissingUrlAndAlias() {
        VaultCredentialRequest request = new VaultCredentialRequest(null, null, "user@google.com", "MySecretPass123!");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> vaultService.createCredential(testEmail, request));
    }

    @Test
    @DisplayName("Reveal Password - Correct Privacy Password")
    void testRevealPassword_CorrectPrivacyPassword() throws Exception {
        RevealPasswordRequest request = new RevealPasswordRequest("Privacy123!");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(vaultRepository.findById(10L)).thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches("Privacy123!", "hashedPrivacy")).thenReturn(true);

        String revealed = vaultService.revealPassword(testEmail, 10L, request);

        assertEquals("MySecretPass123!", revealed);
    }

    @Test
    @DisplayName("Reveal Password - Incorrect Privacy Password")
    void testRevealPassword_IncorrectPrivacyPassword() {
        RevealPasswordRequest request = new RevealPasswordRequest("WrongPrivacy");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(vaultRepository.findById(10L)).thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches("WrongPrivacy", "hashedPrivacy")).thenReturn(false);
        when(passwordEncoder.matches("WrongPrivacy", "hashedPassword")).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> vaultService.revealPassword(testEmail, 10L, request));
    }

    @Test
    @DisplayName("Get User Credentials - Returns List")
    void testGetUserCredentials() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(vaultRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(testCredential));

        List<VaultCredentialResponse> list = vaultService.getUserCredentials(testEmail, null);

        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).getId());
    }

    @Test
    @DisplayName("Delete Credential - Success")
    void testDeleteCredential_Success() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(vaultRepository.findById(10L)).thenReturn(Optional.of(testCredential));

        vaultService.deleteCredential(testEmail, 10L);

        verify(shareRepository, times(1)).deleteByCredential(testCredential);
        verify(vaultRepository, times(1)).delete(testCredential);
    }
}
