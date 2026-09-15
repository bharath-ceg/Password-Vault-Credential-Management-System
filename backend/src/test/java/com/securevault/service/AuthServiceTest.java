package com.securevault.service;

import com.securevault.dto.request.*;
import com.securevault.dto.response.JwtAuthResponse;
import com.securevault.entity.PasswordResetOtp;
import com.securevault.entity.User;
import com.securevault.entity.VerificationToken;
import com.securevault.exception.BadRequestException;
import com.securevault.exception.InvalidOtpException;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.repository.PasswordResetOtpRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VerificationTokenRepository;
import com.securevault.security.JwtTokenProvider;
import com.securevault.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private PasswordResetOtpRepository otpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityMonitoringService securityMonitoringService;

    @Mock
    private com.securevault.service.NotificationService notificationService;

    private JwtTokenProvider tokenProvider = new JwtTokenProvider();

    private AuthServiceImpl authService;

    private User testUser;
    private final String testEmail = "test@securevault.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "ThisIsA512BitLongSuperSecretKeyForSecureVaultJwtTokenProviderRequirement1234567890!");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 86400000L);

        authService = new AuthServiceImpl(
                userRepository,
                tokenRepository,
                otpRepository,
                passwordEncoder,
                authenticationManager,
                tokenProvider,
                emailService,
                securityMonitoringService,
                notificationService
        );

        testUser = User.builder()
                .id(1L)
                .fullName("Secure User")
                .email(testEmail)
                .passwordHash("encodedPassword")
                .privacyPasswordHash("encodedPrivacy")
                .isEmailVerified(true)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Register User - Success")
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("Secure User", testEmail, "Password123!");

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

        authService.registerUser(request);

        verify(tokenRepository, times(1)).deleteByEmail(testEmail);
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(eq(testEmail), eq("Secure User"), anyString());
    }

    @Test
    @DisplayName("Register User - Duplicate Verified Email Throws Exception")
    void testRegisterUser_DuplicateVerifiedEmail() {
        RegisterRequest request = new RegisterRequest("Secure User", testEmail, "Password123!");

        when(userRepository.existsByEmail(testEmail)).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> authService.registerUser(request));
    }

    @Test
    @DisplayName("Login - Success (Records Audit Event LOGIN_SUCCESS)")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest(testEmail, "Password123!");

        Authentication auth = new UsernamePasswordAuthenticationToken(testEmail, "Password123!", java.util.Collections.emptyList());

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        JwtAuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        verify(securityMonitoringService, times(1)).recordLoginAttempt(testUser, testEmail, true);
    }

    @Test
    @DisplayName("Login - Wrong Password (Records Audit Event LOGIN_FAILED)")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest(testEmail, "WrongPassword");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.login(request));

        verify(securityMonitoringService, times(1)).recordLoginAttempt(testUser, testEmail, false);
    }

    @Test
    @DisplayName("Login - Non-existent User (Records Audit Event LOGIN_FAILED)")
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest("unknown@securevault.com", "Password123!");

        when(userRepository.findByEmail("unknown@securevault.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(request));

        verify(securityMonitoringService, times(1)).recordLoginAttempt(null, "unknown@securevault.com", false);
    }

    @Test
    @DisplayName("Login - Unverified Email (Records Audit Event LOGIN_FAILED)")
    void testLogin_UnverifiedEmail() {
        testUser.setIsEmailVerified(false);

        LoginRequest request = new LoginRequest(testEmail, "Password123!");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> authService.login(request));

        verify(securityMonitoringService, times(1)).recordLoginAttempt(testUser, testEmail, false);
    }

    @Test
    @DisplayName("Forgot Password - Valid Email")
    void testProcessForgotPassword_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest(testEmail);

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        authService.processForgotPassword(request);

        verify(otpRepository, times(1)).deleteByUser(testUser);
        verify(otpRepository, times(1)).save(any(PasswordResetOtp.class));
        verify(emailService, times(1)).sendOtpEmail(eq(testEmail), eq("Secure User"), anyString());
    }

    @Test
    @DisplayName("Forgot Password - Unknown Email")
    void testProcessForgotPassword_UnknownEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@securevault.com");

        when(userRepository.findByEmail("unknown@securevault.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.processForgotPassword(request));
    }

    @Test
    @DisplayName("Verify OTP - Success")
    void testVerifyOtp_Success() {
        VerifyOtpRequest request = new VerifyOtpRequest(testEmail, "123456");

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(testUser)
                .otpCode("123456")
                .expiryTime(ZonedDateTime.now().plusMinutes(10))
                .isUsed(false)
                .build();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpRepository.findTopByUserAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(testUser, "123456"))
                .thenReturn(Optional.of(otp));

        boolean result = authService.verifyOtp(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Verify OTP - Incorrect Code")
    void testVerifyOtp_IncorrectCode() {
        VerifyOtpRequest request = new VerifyOtpRequest(testEmail, "999999");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpRepository.findTopByUserAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(testUser, "999999"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidOtpException.class, () -> authService.verifyOtp(request));
    }
}
