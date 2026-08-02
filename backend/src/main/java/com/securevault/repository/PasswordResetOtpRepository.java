package com.securevault.repository;

import com.securevault.entity.PasswordResetOtp;
import com.securevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByUserAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(User user, String otpCode);
    void deleteByUser(User user);
}
