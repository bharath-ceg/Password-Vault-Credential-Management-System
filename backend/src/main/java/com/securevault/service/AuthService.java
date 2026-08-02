package com.securevault.service;

import com.securevault.dto.request.*;
import com.securevault.dto.response.EmailVerificationResponse;
import com.securevault.dto.response.JwtAuthResponse;
import com.securevault.dto.response.UserResponse;

public interface AuthService {
    void registerUser(RegisterRequest request);
    EmailVerificationResponse verifyEmail(String token);
    void resendVerificationEmail(String email);
    JwtAuthResponse login(LoginRequest request);
    void processForgotPassword(ForgotPasswordRequest request);
    boolean verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
    UserResponse getCurrentUser(String email);
}
