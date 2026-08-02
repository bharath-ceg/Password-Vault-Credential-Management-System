package com.securevault.service;

import com.securevault.dto.request.RevealPasswordRequest;
import com.securevault.dto.request.SetPrivacyPasswordRequest;
import com.securevault.dto.request.VaultCredentialRequest;
import com.securevault.dto.response.VaultCredentialResponse;
import com.securevault.entity.enums.CredentialCategory;

import com.securevault.dto.request.ChangePrivacyPasswordRequest;

import java.util.List;

public interface VaultService {
    VaultCredentialResponse createCredential(String userEmail, VaultCredentialRequest request);
    List<VaultCredentialResponse> getUserCredentials(String userEmail, CredentialCategory category);
    List<VaultCredentialResponse> searchCredentials(String userEmail, String query);
    VaultCredentialResponse updateCredential(String userEmail, Long credentialId, VaultCredentialRequest request);
    void deleteCredential(String userEmail, Long credentialId);
    String revealPassword(String userEmail, Long credentialId, RevealPasswordRequest request);
    boolean hasPrivacyPassword(String userEmail);
    void setPrivacyPassword(String userEmail, SetPrivacyPasswordRequest request);
    void changePrivacyPassword(String userEmail, ChangePrivacyPasswordRequest request);
}
