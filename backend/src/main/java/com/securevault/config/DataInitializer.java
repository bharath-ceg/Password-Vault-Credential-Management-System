package com.securevault.config;

import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.entity.enums.CredentialCategory;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.util.AESEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final VaultCredentialRepository vaultRepository;
    private final PasswordEncoder passwordEncoder;
    private final AESEncryptionUtil aesUtil;

    public DataInitializer(UserRepository userRepository,
                           VaultCredentialRepository vaultRepository,
                           PasswordEncoder passwordEncoder,
                           AESEncryptionUtil aesUtil) {
        this.userRepository = userRepository;
        this.vaultRepository = vaultRepository;
        this.passwordEncoder = passwordEncoder;
        this.aesUtil = aesUtil;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String testEmail = "admin@securevault.com";

        if (!userRepository.existsByEmail(testEmail)) {
            // Seed Demo Verified User
            User user = User.builder()
                    .fullName("Enterprise Admin")
                    .email(testEmail)
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .privacyPasswordHash(passwordEncoder.encode("Privacy123!"))
                    .isEmailVerified(true)
                    .build();

            User savedUser = userRepository.save(user);

            // Seed Sample Vault Credentials (AES-256-GCM Encrypted)
            VaultCredential item1 = VaultCredential.builder()
                    .user(savedUser)
                    .applicationUrl("https://instagram.com")
                    .aliasName("Instagram Official")
                    .username("secure_admin")
                    .encryptedPassword(aesUtil.encrypt("InstaPass2026!"))
                    .category(CredentialCategory.SOCIAL_MEDIA)
                    .build();

            VaultCredential item2 = VaultCredential.builder()
                    .user(savedUser)
                    .applicationUrl("https://github.com")
                    .aliasName("GitHub Enterprise")
                    .username("dev_architect")
                    .encryptedPassword(aesUtil.encrypt("GitVault#99"))
                    .category(CredentialCategory.DEVELOPER)
                    .build();

            VaultCredential item3 = VaultCredential.builder()
                    .user(savedUser)
                    .applicationUrl("https://netbanking.hdfcbank.com")
                    .aliasName("HDFC NetBanking")
                    .username("hdfc_user_99")
                    .encryptedPassword(aesUtil.encrypt("BankSecret$2026"))
                    .category(CredentialCategory.BANKING)
                    .build();

            vaultRepository.save(item1);
            vaultRepository.save(item2);
            vaultRepository.save(item3);

            log.info("================================================================================");
            log.info("🚀 [DEMO TEST ACCOUNT INITIALIZED]");
            log.info("👉 Email:            admin@securevault.com");
            log.info("👉 Master Password:  Password123!");
            log.info("👉 Privacy Password: Privacy123!");
            log.info("================================================================================");
        }
    }
}
