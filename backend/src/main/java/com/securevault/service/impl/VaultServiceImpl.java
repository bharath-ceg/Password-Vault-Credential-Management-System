package com.securevault.service.impl;

import com.securevault.dto.request.ChangePrivacyPasswordRequest;
import com.securevault.dto.request.RevealPasswordRequest;
import com.securevault.dto.request.SetPrivacyPasswordRequest;
import com.securevault.dto.request.VaultCredentialRequest;
import com.securevault.dto.response.VaultCredentialResponse;
import com.securevault.entity.CredentialShare;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.entity.enums.CredentialCategory;
import com.securevault.entity.enums.SharePermission;
import com.securevault.exception.BadRequestException;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.CredentialShareRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.service.SecurityMonitoringService;
import com.securevault.service.VaultService;
import com.securevault.util.AESEncryptionUtil;
import com.securevault.util.CategoryAutoDetectorUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaultServiceImpl implements VaultService {

    private final VaultCredentialRepository vaultRepository;
    private final UserRepository userRepository;
    private final CredentialShareRepository shareRepository;
    private final AESEncryptionUtil aesUtil;
    private final CategoryAutoDetectorUtil categoryDetector;
    private final PasswordEncoder passwordEncoder;
    private final SecurityMonitoringService securityMonitoringService;

    public VaultServiceImpl(VaultCredentialRepository vaultRepository,
                            UserRepository userRepository,
                            CredentialShareRepository shareRepository,
                            AESEncryptionUtil aesUtil,
                            CategoryAutoDetectorUtil categoryDetector,
                            PasswordEncoder passwordEncoder,
                            SecurityMonitoringService securityMonitoringService) {
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
        this.aesUtil = aesUtil;
        this.categoryDetector = categoryDetector;
        this.passwordEncoder = passwordEncoder;
        this.securityMonitoringService = securityMonitoringService;
    }

    private void validateUrlOrAlias(String applicationUrl, String aliasName) {
        boolean hasUrl = applicationUrl != null && !applicationUrl.trim().isEmpty();
        boolean hasAlias = aliasName != null && !aliasName.trim().isEmpty();
        if (!hasUrl && !hasAlias) {
            throw new BadRequestException("Please provide either an Application URL or an Alias Name.");
        }
    }

    @Override
    @Transactional
    public VaultCredentialResponse createCredential(String userEmail, VaultCredentialRequest request) {
        User user = getUser(userEmail);
        validateUrlOrAlias(request.getApplicationUrl(), request.getAliasName());

        String appUrl = request.getApplicationUrl() != null ? request.getApplicationUrl().trim() : "";
        String alias = request.getAliasName() != null ? request.getAliasName().trim() : "";

        // Auto-detect category from Application URL or fallback to alias
        CredentialCategory detectedCategory = categoryDetector.detectCategory(appUrl);

        // Encrypt password using AES-256-GCM
        String encryptedPassword = aesUtil.encrypt(request.getPassword());

        VaultCredential credential = VaultCredential.builder()
                .user(user)
                .applicationUrl(appUrl)
                .aliasName(alias)
                .username(request.getUsername().trim())
                .encryptedPassword(encryptedPassword)
                .category(detectedCategory)
                .build();

        VaultCredential saved = vaultRepository.save(credential);
        securityMonitoringService.recordAuditLog(user, userEmail, "VAULT_ACCESS", "Created vault credential: " + (alias.isEmpty() ? appUrl : alias));
        return mapToResponse(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultCredentialResponse> getUserCredentials(String userEmail, CredentialCategory category) {
        User user = getUser(userEmail);
        List<VaultCredential> credentials;
        if (category != null) {
            credentials = new java.util.ArrayList<>(vaultRepository.findByUserAndCategoryOrderByCreatedAtDesc(user, category));
        } else {
            credentials = new java.util.ArrayList<>(vaultRepository.findByUserOrderByCreatedAtDesc(user));
        }

        // Include credentials shared with user having active FULL_MANAGEMENT permission
        List<CredentialShare> sharedWithMe = shareRepository.findByRecipientOrderByCreatedAtDesc(user);
        for (CredentialShare share : sharedWithMe) {
            if (!share.isExpired() && share.getPermission() == SharePermission.FULL_MANAGEMENT) {
                VaultCredential cred = share.getCredential();
                if ((category == null || cred.getCategory() == category) && !credentials.contains(cred)) {
                    credentials.add(cred);
                }
            }
        }

        return credentials.stream().map(c -> mapToResponse(c, null)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultCredentialResponse> searchCredentials(String userEmail, String query) {
        User user = getUser(userEmail);
        if (query == null || query.trim().isEmpty()) {
            return getUserCredentials(userEmail, null);
        }

        List<VaultCredential> credentials = vaultRepository.searchUserCredentials(user, query.trim());
        return credentials.stream().map(c -> mapToResponse(c, null)).toList();
    }

    @Override
    @Transactional
    public VaultCredentialResponse updateCredential(String userEmail, Long credentialId, VaultCredentialRequest request) {
        User user = getUser(userEmail);
        validateUrlOrAlias(request.getApplicationUrl(), request.getAliasName());

        VaultCredential credential = vaultRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with ID: " + credentialId));

        if (!credential.getUser().getId().equals(user.getId())) {
            CredentialShare share = shareRepository.findByCredentialAndRecipient(credential, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Credential not found or not shared with you."));

            if (share.isExpired()) {
                throw new BadRequestException("This credential share has expired.");
            }

            if (share.getPermission() == SharePermission.VIEW_ONLY) {
                throw new UnauthorizedAccessException("View Only permission does not allow editing this credential.");
            }
        }

        String appUrl = request.getApplicationUrl() != null ? request.getApplicationUrl().trim() : "";
        String alias = request.getAliasName() != null ? request.getAliasName().trim() : "";

        CredentialCategory detectedCategory = categoryDetector.detectCategory(appUrl);
        String encryptedPassword = aesUtil.encrypt(request.getPassword());

        credential.setApplicationUrl(appUrl);
        credential.setAliasName(alias);
        credential.setUsername(request.getUsername().trim());
        credential.setEncryptedPassword(encryptedPassword);
        credential.setCategory(detectedCategory);

        VaultCredential updated = vaultRepository.save(credential);
        securityMonitoringService.recordAuditLog(user, userEmail, "VAULT_ACCESS", "Updated vault credential ID: " + credentialId);
        return mapToResponse(updated, null);
    }

    @Override
    @Transactional
    public void deleteCredential(String userEmail, Long credentialId) {
        User user = getUser(userEmail);
        VaultCredential credential = vaultRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with ID: " + credentialId));

        if (!credential.getUser().getId().equals(user.getId())) {
            CredentialShare share = shareRepository.findByCredentialAndRecipient(credential, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Credential not found or not shared with you."));

            if (share.isExpired()) {
                throw new BadRequestException("This credential share has expired.");
            }

            if (share.getPermission() != SharePermission.FULL_MANAGEMENT) {
                throw new UnauthorizedAccessException("Full Management permission is required to delete this credential.");
            }
        }

        // Transactionally delete all associated sharing records before deleting the vault credential
        shareRepository.deleteByCredential(credential);
        vaultRepository.delete(credential);
        securityMonitoringService.recordAuditLog(user, userEmail, "VAULT_ACCESS", "Deleted vault credential ID: " + credentialId);
    }

    @Override
    @Transactional
    public String revealPassword(String userEmail, Long credentialId, RevealPasswordRequest request) {
        User user = getUser(userEmail);

        VaultCredential credential = vaultRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with ID: " + credentialId));

        if (!credential.getUser().getId().equals(user.getId())) {
            CredentialShare share = shareRepository.findByCredentialAndRecipient(credential, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Credential not found or not shared with you."));

            if (share.isExpired()) {
                throw new BadRequestException("This credential share has expired.");
            }
        }

        // Check Privacy Password or fallback to account password verification
        String targetHash = user.getPrivacyPasswordHash() != null
                ? user.getPrivacyPasswordHash()
                : user.getPasswordHash();

        if (!passwordEncoder.matches(request.getPrivacyPassword(), targetHash)) {
            throw new UnauthorizedAccessException("Incorrect Privacy Password.");
        }

        // Decrypt password using AES-256-GCM
        String decrypted = aesUtil.decrypt(credential.getEncryptedPassword());
        securityMonitoringService.recordAuditLog(user, userEmail, "VAULT_ACCESS", "Revealed password for credential ID: " + credentialId);
        return decrypted;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPrivacyPassword(String userEmail) {
        User user = getUser(userEmail);
        return user.getPrivacyPasswordHash() != null && !user.getPrivacyPasswordHash().trim().isEmpty();
    }

    @Override
    @Transactional
    public void setPrivacyPassword(String userEmail, SetPrivacyPasswordRequest request) {
        User user = getUser(userEmail);
        user.setPrivacyPasswordHash(passwordEncoder.encode(request.getPrivacyPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePrivacyPassword(String userEmail, ChangePrivacyPasswordRequest request) {
        User user = getUser(userEmail);

        // 1. Verify Account Login Password
        if (!passwordEncoder.matches(request.getLoginPassword(), user.getPasswordHash())) {
            throw new UnauthorizedAccessException("Incorrect account login password.");
        }

        // 2. Ensure the new Privacy Password is different from the current one
        if (user.getPrivacyPasswordHash() != null &&
                passwordEncoder.matches(request.getNewPrivacyPassword(), user.getPrivacyPasswordHash())) {
            throw new IllegalArgumentException(
                    "The new privacy password cannot be the same as your current privacy password.");
        }

        // 3. Save new Privacy Password hash using BCrypt
        user.setPrivacyPasswordHash(passwordEncoder.encode(request.getNewPrivacyPassword()));
        userRepository.save(user);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private VaultCredentialResponse mapToResponse(VaultCredential credential, String revealedPassword) {
        return VaultCredentialResponse.builder()
                .id(credential.getId())
                .applicationUrl(credential.getApplicationUrl())
                .aliasName(credential.getAliasName())
                .username(credential.getUsername())
                .maskedPassword("••••••••••••")
                .revealedPassword(revealedPassword)
                .category(credential.getCategory())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .build();
    }
}
