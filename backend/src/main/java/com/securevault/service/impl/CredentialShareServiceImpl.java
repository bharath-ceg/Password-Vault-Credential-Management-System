package com.securevault.service.impl;

import com.securevault.dto.request.CreateShareRequest;
import com.securevault.dto.request.RevealPasswordRequest;
import com.securevault.dto.request.UpdateShareRequest;
import com.securevault.dto.response.CredentialShareResponse;
import com.securevault.entity.CredentialShare;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.entity.enums.SharePermission;
import com.securevault.entity.enums.NotificationType;
import com.securevault.exception.BadRequestException;
import com.securevault.exception.ResourceNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.repository.CredentialShareRepository;
import com.securevault.repository.UserRepository;
import com.securevault.repository.VaultCredentialRepository;
import com.securevault.service.CredentialShareService;
import com.securevault.service.NotificationService;
import com.securevault.service.SecurityMonitoringService;
import com.securevault.util.AESEncryptionUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class CredentialShareServiceImpl implements CredentialShareService {

    private final CredentialShareRepository shareRepository;
    private final VaultCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final AESEncryptionUtil aesUtil;
    private final PasswordEncoder passwordEncoder;
    private final SecurityMonitoringService securityMonitoringService;
    private final NotificationService notificationService;

    public CredentialShareServiceImpl(CredentialShareRepository shareRepository,
                                       VaultCredentialRepository credentialRepository,
                                       UserRepository userRepository,
                                       AESEncryptionUtil aesUtil,
                                       PasswordEncoder passwordEncoder,
                                       SecurityMonitoringService securityMonitoringService,
                                       NotificationService notificationService) {
        this.shareRepository = shareRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.aesUtil = aesUtil;
        this.passwordEncoder = passwordEncoder;
        this.securityMonitoringService = securityMonitoringService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CredentialShareResponse createShare(String currentUserEmail, CreateShareRequest request) {
        User currentUser = getUser(currentUserEmail);

        VaultCredential credential = credentialRepository.findById(request.getCredentialId())
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with ID: " + request.getCredentialId()));

        User credentialOwner = credential.getUser();

        // Verify that current user is either the credential owner OR has active FULL_MANAGEMENT permission
        if (!credentialOwner.getId().equals(currentUser.getId())) {
            CredentialShare parentShare = shareRepository.findByCredentialAndRecipient(credential, currentUser)
                    .orElseThrow(() -> new UnauthorizedAccessException("You are not authorized to share this credential."));

            if (parentShare.isExpired()) {
                throw new BadRequestException("Your shared access to this credential has expired.");
            }
            if (parentShare.getPermission() != SharePermission.FULL_MANAGEMENT) {
                throw new UnauthorizedAccessException("Full Management permission is required to manage sharing for this credential.");
            }
        }

        String recipientEmail = request.getRecipientEmail().trim().toLowerCase();
        if (credentialOwner.getEmail().equalsIgnoreCase(recipientEmail)) {
            throw new BadRequestException("Cannot share a credential with its owner.");
        }
        if (currentUser.getEmail().equalsIgnoreCase(recipientEmail)) {
            throw new BadRequestException("You cannot share a credential with yourself.");
        }

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not registered: " + request.getRecipientEmail()));

        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new BadRequestException("Expiration date/time must be in the future.");
        }

        // Always associate the original credential owner as the share owner in the database
        CredentialShare share = shareRepository.findByCredentialAndRecipient(credential, recipient)
                .orElseGet(() -> CredentialShare.builder()
                        .credential(credential)
                        .owner(credentialOwner)
                        .recipient(recipient)
                        .build());

        share.setPermission(request.getPermission());
        share.setExpiresAt(request.getExpiresAt());

        CredentialShare saved = shareRepository.save(share);
        securityMonitoringService.recordAuditLog(currentUser, currentUserEmail, "CREDENTIAL_SHARING", "Shared credential with user: " + recipientEmail + " (Permission: " + request.getPermission() + ")");

        String alias = credential.getAliasName() != null && !credential.getAliasName().isEmpty()
                ? credential.getAliasName()
                : (credential.getApplicationUrl() != null ? credential.getApplicationUrl() : "Credential");

        String message = "A credential ('" + alias + "') has been securely shared with you by " + currentUser.getFullName() + ".";
        String refId = "SHARE_" + saved.getId();

        notificationService.createNotification(
                recipient,
                NotificationType.CREDENTIAL_SHARED,
                "Credential shared with you",
                message,
                refId
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialShareResponse> getMyShares(String userEmail) {
        User user = getUser(userEmail);
        List<CredentialShare> shares = new java.util.ArrayList<>(shareRepository.findByOwnerOrderByCreatedAtDesc(user));

        // Include shares for credentials where user has active FULL_MANAGEMENT permission
        List<CredentialShare> receivedShares = shareRepository.findByRecipientOrderByCreatedAtDesc(user);
        for (CredentialShare receivedShare : receivedShares) {
            if (!receivedShare.isExpired() && receivedShare.getPermission() == SharePermission.FULL_MANAGEMENT) {
                VaultCredential managedCred = receivedShare.getCredential();
                List<CredentialShare> credShares = shareRepository.findByCredential(managedCred);
                for (CredentialShare s : credShares) {
                    if (!s.getRecipient().getId().equals(user.getId()) && !shares.contains(s)) {
                        shares.add(s);
                    }
                }
            }
        }

        return shares.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialShareResponse> getSharedWithMe(String recipientEmail) {
        User recipient = getUser(recipientEmail);
        List<CredentialShare> shares = shareRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        return shares.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public CredentialShareResponse updateShare(String userEmail, Long shareId, UpdateShareRequest request) {
        User user = getUser(userEmail);
        CredentialShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential share not found with ID: " + shareId));

        boolean isOwner = share.getOwner().getId().equals(user.getId()) || share.getCredential().getUser().getId().equals(user.getId());
        if (!isOwner) {
            CredentialShare userShare = shareRepository.findByCredentialAndRecipient(share.getCredential(), user)
                    .orElseThrow(() -> new UnauthorizedAccessException("You do not have permission to update this share."));
            if (userShare.isExpired() || userShare.getPermission() != SharePermission.FULL_MANAGEMENT) {
                throw new UnauthorizedAccessException("Full Management permission is required to update sharing.");
            }
        }

        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new BadRequestException("Expiration date/time must be in the future.");
        }

        share.setPermission(request.getPermission());
        share.setExpiresAt(request.getExpiresAt());

        CredentialShare updated = shareRepository.save(share);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void revokeShare(String userEmail, Long shareId) {
        User user = getUser(userEmail);
        CredentialShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential share not found with ID: " + shareId));

        boolean isOwner = share.getOwner().getId().equals(user.getId()) || share.getCredential().getUser().getId().equals(user.getId());
        boolean isRecipient = share.getRecipient().getId().equals(user.getId());
        boolean isManager = false;

        if (!isOwner && !isRecipient) {
            CredentialShare userShare = shareRepository.findByCredentialAndRecipient(share.getCredential(), user).orElse(null);
            if (userShare != null && !userShare.isExpired() && userShare.getPermission() == SharePermission.FULL_MANAGEMENT) {
                isManager = true;
            }
        }

        if (!isOwner && !isRecipient && !isManager) {
            throw new UnauthorizedAccessException("You do not have permission to revoke this share.");
        }

        shareRepository.delete(share);
    }

    @Override
    @Transactional(readOnly = true)
    public String revealSharedPassword(String recipientEmail, Long shareId, RevealPasswordRequest request) {
        User recipient = getUser(recipientEmail);
        CredentialShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential share not found with ID: " + shareId));

        if (!share.getRecipient().getId().equals(recipient.getId()) && !share.getOwner().getId().equals(recipient.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to view this shared credential.");
        }

        if (share.isExpired()) {
            throw new BadRequestException("This credential share has expired.");
        }

        // Verify recipient's Privacy Password (or account login password fallback)
        String targetHash = recipient.getPrivacyPasswordHash() != null
                ? recipient.getPrivacyPasswordHash()
                : recipient.getPasswordHash();

        if (!passwordEncoder.matches(request.getPrivacyPassword(), targetHash)) {
            throw new UnauthorizedAccessException("Incorrect Privacy Password.");
        }

        // Decrypt password using AES-256-GCM
        return aesUtil.decrypt(share.getCredential().getEncryptedPassword());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private CredentialShareResponse mapToResponse(CredentialShare share) {
        VaultCredential cred = share.getCredential();
        String alias = cred.getAliasName() != null && !cred.getAliasName().isEmpty() ? cred.getAliasName() : cred.getApplicationUrl();
        String url = cred.getApplicationUrl() != null ? cred.getApplicationUrl() : "";

        return CredentialShareResponse.builder()
                .id(share.getId())
                .credentialId(cred.getId())
                .credentialAlias(alias)
                .credentialUrl(url)
                .username(cred.getUsername())
                .category(cred.getCategory())
                .ownerEmail(share.getOwner().getEmail())
                .ownerFullName(share.getOwner().getFullName())
                .recipientEmail(share.getRecipient().getEmail())
                .recipientFullName(share.getRecipient().getFullName())
                .permission(share.getPermission())
                .createdAt(share.getCreatedAt())
                .expiresAt(share.getExpiresAt())
                .isExpired(share.isExpired())
                .build();
    }
}
