package com.securevault.service.impl;

import com.securevault.dto.request.*;
import com.securevault.dto.response.EmailVerificationResponse;
import com.securevault.dto.response.JwtAuthResponse;
import com.securevault.dto.response.UserResponse;
import com.securevault.entity.PasswordResetOtp;
import com.securevault.entity.User;
import com.securevault.entity.VerificationToken;
import com.securevault.entity.enums.NotificationType;
import com.securevault.exception.BadRequestException;
import com.securevault.exception.InvalidOtpException;
import com.securevault.exception.InvalidTokenException;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.repository.PasswordResetOtpRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VerificationTokenRepository;
import com.securevault.security.JwtTokenProvider;
import com.securevault.service.AuthService;
import com.securevault.service.EmailService;
import com.securevault.service.NotificationService;
import com.securevault.service.SecurityMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final SecurityMonitoringService securityMonitoringService;
    private final NotificationService notificationService;

    public AuthServiceImpl(UserRepository userRepository,
                           VerificationTokenRepository tokenRepository,
                           PasswordResetOtpRepository otpRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider,
                           EmailService emailService,
                           SecurityMonitoringService securityMonitoringService,
                           NotificationService notificationService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
        this.securityMonitoringService = securityMonitoringService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            User existingUser = userRepository.findByEmail(email).orElse(null);
            if (existingUser != null && Boolean.TRUE.equals(existingUser.getIsEmailVerified())) {
                throw new BadRequestException("An account with email '" + email + "' already exists.");
            }
        }

        tokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .email(email)
                .fullName(request.getFullName().trim())
                .passwordHash(hashedPassword)
                .expiryDate(ZonedDateTime.now().plusHours(24))
                .isUsed(false)
                .build();

        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(email, request.getFullName(), token);
        log.info("Pending registration created for email: {}", email);
    }

    @Override
    @Transactional
    public EmailVerificationResponse verifyEmail(String tokenStr) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(tokenStr);

        if (tokenOpt.isEmpty()) {
            return new EmailVerificationResponse(
                    "INVALID",
                    "The verification link is invalid. Please register again or request another verification email.",
                    null
            );
        }

        VerificationToken token = tokenOpt.get();

        User user = userRepository.findByEmail(token.getEmail()).orElse(null);
        if (Boolean.TRUE.equals(token.getIsUsed()) || (user != null && Boolean.TRUE.equals(user.getIsEmailVerified()))) {
            return new EmailVerificationResponse(
                    "ALREADY_VERIFIED",
                    "Your email has already been verified.",
                    token.getEmail()
            );
        }

        if (token.isExpired()) {
            return new EmailVerificationResponse(
                    "EXPIRED",
                    "Please request another verification email.",
                    token.getEmail()
            );
        }

        // Activate User
        if (user == null) {
            user = User.builder()
                    .fullName(token.getFullName())
                    .email(token.getEmail())
                    .passwordHash(token.getPasswordHash())
                    .isEmailVerified(true)
                    .build();
        } else {
            user.setIsEmailVerified(true);
        }

        userRepository.save(user);

        token.setIsUsed(true);
        tokenRepository.save(token);

        log.info("User email verified and account activated for: {}", token.getEmail());

        return new EmailVerificationResponse(
                "SUCCESS",
                "Your email has been verified successfully. Your SecureVault account is now active. You can now log in using your registered email and password.",
                token.getEmail()
        );
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String emailStr) {
        String email = emailStr.toLowerCase().trim();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new BadRequestException("Your email has already been verified.");
        }

        tokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        String fullName = user != null ? user.getFullName() : "User";
        String hashedPassword = user != null ? user.getPasswordHash() : "";

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .email(email)
                .fullName(fullName)
                .passwordHash(hashedPassword)
                .expiryDate(ZonedDateTime.now().plusHours(24))
                .isUsed(false)
                .build();

        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(email, fullName, token);
        log.info("Verification email resent to {}", email);
    }

    @Override
    public JwtAuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            securityMonitoringService.recordLoginAttempt(null, email, false);
            throw new BadRequestException("Invalid email or password.");
        }

        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
            securityMonitoringService.recordLoginAttempt(user, email, false);
            throw new BadRequestException("Your email address has not been verified yet. Please check your inbox for the verification link.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            securityMonitoringService.recordLoginAttempt(user, email, false);
            throw new BadRequestException("Invalid email or password.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);

            securityMonitoringService.recordLoginAttempt(user, email, true);

            notificationService.createNotification(
                    user,
                    NotificationType.SUCCESSFUL_LOGIN,
                    "New login detected",
                    "A successful login was detected on your SecureVault account.",
                    null
            );

            return JwtAuthResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .user(mapToUserResponse(user))
                    .build();
        } catch (Exception e) {
            securityMonitoringService.recordLoginAttempt(user, email, false);
            throw e;
        }
    }

    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No registered account found with email: " + email));

        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new BadRequestException("Account email is not verified.");
        }

        otpRepository.deleteByUser(user);

        String otpCode = String.format("%06d", new SecureRandom().nextInt(1000000));

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .otpCode(otpCode)
                .expiryTime(ZonedDateTime.now().plusMinutes(5))
                .isUsed(false)
                .build();

        otpRepository.save(otp);

        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otpCode);
        log.info("Password reset OTP generated and emailed to {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        PasswordResetOtp otp = otpRepository.findTopByUserAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(user, request.getOtpCode())
                .orElseThrow(() -> new InvalidOtpException("Invalid or Expired OTP."));

        if (otp.isExpired()) {
            throw new InvalidOtpException("Invalid or Expired OTP.");
        }

        return true;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match.");
        }

        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        PasswordResetOtp otp = otpRepository.findTopByUserAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(user, request.getOtpCode())
                .orElseThrow(() -> new InvalidOtpException("Invalid or Expired OTP."));

        if (otp.isExpired()) {
            throw new InvalidOtpException("Invalid or Expired OTP.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password cannot be the same as your old password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otp.setIsUsed(true);
        otpRepository.save(otp);

        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFullName());
        log.info("Password reset successfully completed for user: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found."));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .isEmailVerified(user.getIsEmailVerified())
                .hasPrivacyPassword(user.getPrivacyPasswordHash() != null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
